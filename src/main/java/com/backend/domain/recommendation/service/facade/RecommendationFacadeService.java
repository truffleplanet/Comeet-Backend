package com.backend.domain.recommendation.service.facade;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.ai.dto.MenuRerankRequest;
import com.backend.common.ai.dto.MenuRerankResponse;
import com.backend.common.ai.dto.RerankRequest;
import com.backend.common.ai.dto.RerankResponse;
import com.backend.common.ai.service.EmbeddingService;
import com.backend.common.ai.service.LlmService;
import com.backend.common.redis.dto.VectorSearchResult;
import com.backend.common.redis.service.RedisVectorService;
import com.backend.common.util.GeoUtils;
import com.backend.domain.bean.dto.common.BeanBadgeDto;
import com.backend.domain.beanscore.dto.response.BeanScoreWithBeanDto;
import com.backend.domain.beanscore.service.query.BeanScoreQueryService;
import com.backend.domain.flavor.converter.FlavorConverter;
import com.backend.domain.flavor.dto.common.FlavorBadgeDto;
import com.backend.domain.flavor.entity.Flavor;
import com.backend.domain.flavor.service.FlavorQueryService;
import com.backend.domain.preference.entity.UserPreference;
import com.backend.domain.preference.service.query.PreferenceQueryService;
import com.backend.domain.recommendation.dto.internal.MenuWithBeanScoreDto;
import com.backend.domain.recommendation.dto.request.RecommendationReqDto;
import com.backend.domain.recommendation.dto.response.BeanRecommendationResDto;
import com.backend.domain.recommendation.dto.response.MenuRecommendationResDto;
import com.backend.domain.recommendation.dto.response.NearbyMenuRecommendationResDto;
import com.backend.domain.recommendation.service.query.RecommendationQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationFacadeService {

	private final PreferenceQueryService preferenceQueryService;
	private final BeanScoreQueryService beanScoreQueryService;
	private final RecommendationQueryService recommendationQueryService;
	private final FlavorQueryService flavorQueryService;
	private final EmbeddingService embeddingService;
	private final RedisVectorService redisVectorService;
	private final LlmService llmService;

	private static final int VECTOR_SEARCH_TOP_K = 20;
	private static final int FINAL_RECOMMENDATION_COUNT = 5;
	private static final int MAX_RADIUS_EXPANSION_ATTEMPTS = 3;
	private static final int MAX_RADIUS_KM = 30;

	@Transactional(readOnly = true)
	public List<BeanRecommendationResDto> recommendBeans(Long userId) {
		log.info("[Recommendation] 원두 추천 시작 - userId: {}", userId);

		UserPreference preference = preferenceQueryService.findByUserId(userId)
			.orElseGet(() -> UserPreference.createDefault(userId));

		List<String> preferredRoastLevels = convertRoastLevelsToStrings(preference);

		List<BeanScoreWithBeanDto> filteredBeans = beanScoreQueryService.findFilteredBeanScores(
			preference.getDislikedTags(),
			preferredRoastLevels);

		if (filteredBeans.isEmpty()) {
			log.warn("[Recommendation] 하드 필터링 후 원두 없음 - userId: {}", userId);
			return List.of();
		}

		log.debug("[Recommendation] 하드 필터링 완료 - 후보: {}건", filteredBeans.size());

		List<Long> beanIds = filteredBeans.stream()
			.map(BeanScoreWithBeanDto::beanId)
			.toList();

		List<VectorSearchResult> vectorResults = performVectorSearch(preference.getLikedTags(), beanIds);

		if (vectorResults.isEmpty()) {
			log.warn("[Recommendation] 벡터 검색 결과 없음 - userId: {}", userId);
			return filteredBeans.stream()
				.limit(FINAL_RECOMMENDATION_COUNT)
				.map(bean -> createBeanRecommendation(
					bean,
					filteredBeans.indexOf(bean) + 1,
					0.0,
					"취향에 맞는 원두입니다.",
					getFlavorBadges(bean.flavorTags())))
				.toList();
		}

		Map<Long, BeanScoreWithBeanDto> beanMap = filteredBeans.stream()
			.collect(Collectors.toMap(BeanScoreWithBeanDto::beanId, b -> b));

		Map<Long, Double> similarityMap = vectorResults.stream()
			.collect(Collectors.toMap(VectorSearchResult::beanId, VectorSearchResult::score));

		List<RerankRequest.CandidateInfo> candidates = vectorResults.stream()
			.filter(vr -> beanMap.containsKey(vr.beanId()))
			.map(vr -> {
				BeanScoreWithBeanDto bean = beanMap.get(vr.beanId());
				return new RerankRequest.CandidateInfo(
					bean.beanId(),
					bean.beanName(),
					bean.roasteryName(),
					bean.country(),
					bean.roastLevel() != null ? bean.roastLevel().name() : "MEDIUM",
					bean.flavorTags() != null ? bean.flavorTags() : List.of(),
					bean.acidity(),
					bean.body(),
					bean.sweetness(),
					bean.bitterness(),
					vr.score());
			})
			.toList();

		RerankRequest rerankRequest = new RerankRequest(
			new RerankRequest.UserPreferenceInfo(
				preference.getPrefAcidity(),
				preference.getPrefBody(),
				preference.getPrefSweetness(),
				preference.getPrefBitterness(),
				preferredRoastLevels,
				preference.getLikedTags() != null ? preference.getLikedTags() : List.of()),
			candidates);

		try {
			RerankResponse rerankResponse = llmService.rerank(rerankRequest);

			return rerankResponse.recommendations().stream()
				.map(rec -> {
					BeanScoreWithBeanDto bean = beanMap.get(rec.beanId());
					if (bean == null) {
						return Optional.<BeanRecommendationResDto>empty();
					}
					Double similarity = similarityMap.getOrDefault(rec.beanId(), 0.0);
					return Optional.of(createBeanRecommendation(
						bean,
						rec.rank(),
						similarity,
						rec.reason(),
						getFlavorBadges(bean.flavorTags())));
				})
				.flatMap(Optional::stream)
				.toList();

		} catch (RuntimeException e) {
			log.error("[Recommendation] LLM 리랭킹 실패, 벡터 검색 결과로 대체", e);
			return vectorResults.stream()
				.limit(FINAL_RECOMMENDATION_COUNT)
				.map(vr -> {
					BeanScoreWithBeanDto bean = beanMap.get(vr.beanId());
					if (bean == null) {
						return Optional.<BeanRecommendationResDto>empty();
					}
					return Optional.of(createBeanRecommendation(
						bean,
						vectorResults.indexOf(vr) + 1,
						vr.score(),
						"취향과 유사한 플레이버를 가진 원두입니다.",
						getFlavorBadges(bean.flavorTags())));
				})
				.flatMap(Optional::stream)
				.toList();
		}
	}

	@Transactional(readOnly = true)
	public List<MenuRecommendationResDto> recommendMenus(Long userId, RecommendationReqDto reqDto) {
		log.info("[Recommendation] 메뉴 추천 시작 - userId: {}, type: {}", userId, reqDto.type());

		UserPreference preference = preferenceQueryService.findByUserId(userId)
			.orElseGet(() -> UserPreference.createDefault(userId));

		GeoUtils.BoundingBox boundingBox = null;
		if (reqDto.isLocal() && reqDto.hasValidLocation()) {
			boundingBox = GeoUtils.calculateBoundingBox(
				reqDto.latitude(),
				reqDto.longitude(),
				reqDto.radiusKm());
			log.debug("[Recommendation] LOCAL 모드 BoundingBox 계산 완료 - 반경: {}km", reqDto.radiusKm());
		}

		List<String> preferredRoastLevels = convertRoastLevelsToStrings(preference);

		List<MenuWithBeanScoreDto> filteredMenus = recommendationQueryService.findFilteredMenus(
			preference.getDislikedTags(),
			preferredRoastLevels,
			boundingBox);

		if (filteredMenus.isEmpty()) {
			log.warn("[Recommendation] 필터링 후 메뉴 없음 - userId: {}", userId);
			return List.of();
		}

		log.debug("[Recommendation] 메뉴 필터링 완료 - 후보: {}건", filteredMenus.size());

		List<Long> beanIds = filteredMenus.stream()
			.map(MenuWithBeanScoreDto::beanId)
			.distinct()
			.toList();

		List<VectorSearchResult> vectorResults = performVectorSearch(preference.getLikedTags(), beanIds);

		Map<Long, Double> beanSimilarityMap = vectorResults.stream()
			.collect(Collectors.toMap(VectorSearchResult::beanId, VectorSearchResult::score));

		List<MenuWithScore> menuWithScores = filteredMenus.stream()
			.map(menu -> new MenuWithScore(
				menu,
				beanSimilarityMap.getOrDefault(menu.beanId(), 0.0),
				reqDto.isLocal() && reqDto.hasValidLocation()
					? recommendationQueryService.calculateDistance(menu, reqDto.latitude(),
					reqDto.longitude()).orElse(null)
					: null))
			.sorted((a, b) -> Double.compare(b.similarity, a.similarity))
			.limit(VECTOR_SEARCH_TOP_K)
			.toList();

		if (menuWithScores.isEmpty()) {
			return List.of();
		}

		List<RerankRequest.MenuCandidateInfo> menuCandidates = menuWithScores.stream()
			.map(ms -> new RerankRequest.MenuCandidateInfo(
				ms.menu.menuId(),
				ms.menu.menuName(),
				ms.menu.menuDescription(),
				ms.menu.beanId(),
				ms.menu.beanName(),
				ms.menu.roasteryName(),
				ms.menu.beanCountry(),
				ms.menu.roastLevel() != null ? ms.menu.roastLevel().name() : "MEDIUM",
				ms.menu.flavorTags() != null ? ms.menu.flavorTags() : List.of(),
				ms.menu.acidity(),
				ms.menu.body(),
				ms.menu.sweetness(),
				ms.menu.bitterness(),
				ms.similarity))
			.toList();

		MenuRerankRequest menuRerankRequest = new MenuRerankRequest(
			new RerankRequest.UserPreferenceInfo(
				preference.getPrefAcidity(),
				preference.getPrefBody(),
				preference.getPrefSweetness(),
				preference.getPrefBitterness(),
				preferredRoastLevels,
				preference.getLikedTags() != null ? preference.getLikedTags() : List.of()),
			menuCandidates);

		try {
			MenuRerankResponse rerankResponse = llmService.rerankMenus(menuRerankRequest);

			Map<Long, MenuWithScore> menuIdToMenuMap = menuWithScores.stream()
				.collect(Collectors.toMap(ms -> ms.menu.menuId(), ms -> ms, (a, b) -> a));

			return rerankResponse.recommendations().stream()
				.map(rec -> {
					MenuWithScore ms = menuIdToMenuMap.get(rec.menuId());
					if (ms == null) {
						return Optional.<MenuRecommendationResDto>empty();
					}
					return Optional.of(createMenuRecommendation(
						ms.menu,
						rec.rank(),
						ms.similarity,
						rec.reason(),
						ms.distance,
						getFlavorBadges(ms.menu.flavorTags())));
				})
				.flatMap(Optional::stream)
				.toList();

		} catch (RuntimeException e) {
			log.error("[Recommendation] LLM 리랭킹 실패, 벡터 검색 결과로 대체", e);
			return menuWithScores.stream()
				.limit(FINAL_RECOMMENDATION_COUNT)
				.map(ms -> createMenuRecommendation(
					ms.menu,
					menuWithScores.indexOf(ms) + 1,
					ms.similarity,
					"취향과 유사한 플레이버의 메뉴입니다.",
					ms.distance,
					getFlavorBadges(ms.menu.flavorTags())))
				.toList();
		}
	}

	@Transactional(readOnly = true)
	public NearbyMenuRecommendationResDto recommendNearbyMenus(Long userId, RecommendationReqDto reqDto) {
		log.info("[Recommendation] 근거리 메뉴 추천 시작 - userId: {}, 반경: {}km", userId, reqDto.radiusKm());

		int requestedRadius = reqDto.radiusKm();
		int currentRadius = requestedRadius;
		int attempts = 0;

		List<MenuRecommendationResDto> recommendations = List.of();

		while (attempts <= MAX_RADIUS_EXPANSION_ATTEMPTS && currentRadius <= MAX_RADIUS_KM) {
			RecommendationReqDto currentReqDto = new RecommendationReqDto(
				reqDto.type(),
				reqDto.latitude(),
				reqDto.longitude(),
				currentRadius);

			recommendations = recommendMenus(userId, currentReqDto);

			if (!recommendations.isEmpty()) {
				log.info("[Recommendation] 추천 결과 발견 - {}건, 반경: {}km", recommendations.size(), currentRadius);
				break;
			}

			attempts++;
			int nextRadius = currentRadius * 2;
			if (nextRadius > MAX_RADIUS_KM) {
				nextRadius = MAX_RADIUS_KM;
			}

			if (currentRadius >= MAX_RADIUS_KM) {
				log.warn("[Recommendation] 최대 반경에서도 메뉴 없음 - 반경: {}km, userId: {}", MAX_RADIUS_KM, userId);
				break;
			}

			log.info("[Recommendation] 반경 확장 - {}km → {}km (시도 {}회)", currentRadius, nextRadius, attempts);
			currentRadius = nextRadius;
		}

		return NearbyMenuRecommendationResDto.builder()
			.recommendations(recommendations)
			.requestedRadiusKm(requestedRadius)
			.actualRadiusKm(currentRadius)
			.radiusExpanded(currentRadius > requestedRadius)
			.build();
	}

	@Transactional(readOnly = true)
	public List<MenuRecommendationResDto> findMenusByBean(Long beanId, RecommendationReqDto reqDto) {
		log.info("[Recommendation] 원두별 메뉴 조회 - beanId: {}, type: {}", beanId, reqDto.type());

		GeoUtils.BoundingBox boundingBox = null;
		if (reqDto.isLocal() && reqDto.hasValidLocation()) {
			boundingBox = GeoUtils.calculateBoundingBox(
				reqDto.latitude(),
				reqDto.longitude(),
				reqDto.radiusKm());
		}

		List<MenuWithBeanScoreDto> menus = recommendationQueryService.findMenusByBeanId(beanId, boundingBox);

		return menus.stream()
			.map(menu -> {
				Double distance = null;
				if (reqDto.isLocal() && reqDto.hasValidLocation()) {
					distance = recommendationQueryService.calculateDistance(
						menu, reqDto.latitude(), reqDto.longitude()).orElse(null);
				}
				return createMenuRecommendation(
					menu,
					null,
					null,
					null,
					distance,
					getFlavorBadges(menu.flavorTags()));
			})
			.toList();
	}

	private List<VectorSearchResult> performVectorSearch(List<String> likedTags, List<Long> beanIds) {
		if (likedTags == null || likedTags.isEmpty()) {
			log.debug("[Recommendation] 선호 태그 없음, 벡터 검색 생략");
			return List.of();
		}

		try {
			List<String> hierarchyPaths = flavorQueryService.getHierarchyPaths(likedTags);
			log.debug("[Recommendation] 플레이버 계층 경로 변환 완료 - {}", hierarchyPaths);

			float[] queryEmbedding = embeddingService.embedTags(hierarchyPaths);
			return redisVectorService.searchSimilarInBeans(queryEmbedding, beanIds, VECTOR_SEARCH_TOP_K);
		} catch (RuntimeException e) {
			log.error("[Recommendation] 벡터 검색 실패", e);
			return List.of();
		}
	}

	private List<String> convertRoastLevelsToStrings(UserPreference preference) {
		if (preference.getPreferredRoastLevels() == null || preference.getPreferredRoastLevels().isEmpty()) {
			return List.of();
		}
		return preference.getPreferredRoastLevels().stream()
			.map(Enum::name)
			.toList();
	}

	private BeanRecommendationResDto createBeanRecommendation(
		BeanScoreWithBeanDto bean, int rank, Double similarity, String reason,
		List<FlavorBadgeDto> flavors
	) {
		return BeanRecommendationResDto.builder()
			.beanId(bean.beanId())
			.beanName(bean.beanName())
			.description(null)
			.origin(bean.country())
			.roastLevel(bean.roastLevel())
			.flavors(flavors)
			.totalScore(bean.totalScore())
			.rank(rank)
			.reason(reason)
			.similarityScore(similarity)
			.build();
	}

	private MenuRecommendationResDto createMenuRecommendation(
		MenuWithBeanScoreDto menu, Integer rank, Double similarity, String reason, Double distance,
		List<FlavorBadgeDto> flavors
	) {
		return MenuRecommendationResDto.builder()
			.menuId(menu.menuId())
			.menuName(menu.menuName())
			.menuDescription(menu.menuDescription())
			.price(menu.menuPrice())
			.menuImageUrl(menu.menuImageUrl())
			.storeId(menu.storeId())
			.storeName(menu.storeName())
			.storeAddress(menu.storeAddress())
			.storeLatitude(menu.storeLatitude())
			.storeLongitude(menu.storeLongitude())
			.distanceKm(distance)
			.beans(List.of(BeanBadgeDto.builder()
				.id(menu.beanId())
				.name(menu.beanName())
				.build()))
			.flavors(flavors)
			.rank(rank)
			.reason(reason)
			.similarityScore(similarity)
			.build();
	}

	private List<FlavorBadgeDto> getFlavorBadges(List<String> flavorCodes) {
		if (flavorCodes == null || flavorCodes.isEmpty()) {
			return List.of();
		}
		List<Flavor> flavors = flavorQueryService.findByCodes(flavorCodes);
		return FlavorConverter.toFlavorBadgeDtoList(flavors);
	}

	private record MenuWithScore(
		MenuWithBeanScoreDto menu,
		Double similarity,
		Double distance) {
	}
}
