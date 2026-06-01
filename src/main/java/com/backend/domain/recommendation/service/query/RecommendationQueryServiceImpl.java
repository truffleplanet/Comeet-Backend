package com.backend.domain.recommendation.service.query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.util.GeoUtils;
import com.backend.domain.recommendation.dto.internal.MenuWithBeanScoreDto;
import com.backend.domain.recommendation.mapper.query.RecommendationQueryMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecommendationQueryServiceImpl implements RecommendationQueryService {

	private final RecommendationQueryMapper recommendationQueryMapper;

	@Override
	public List<MenuWithBeanScoreDto> findFilteredMenus(
		List<String> dislikedTags,
		List<String> preferredRoastLevels,
		GeoUtils.BoundingBox boundingBox
	) {
		BigDecimal minLat = null, maxLat = null, minLon = null, maxLon = null;
		if (boundingBox != null) {
			minLat = boundingBox.minLatitude();
			maxLat = boundingBox.maxLatitude();
			minLon = boundingBox.minLongitude();
			maxLon = boundingBox.maxLongitude();
		}

		List<MenuWithBeanScoreDto> menus = recommendationQueryMapper.findFilteredMenus(
			dislikedTags,
			preferredRoastLevels,
			minLat, maxLat, minLon, maxLon);

		log.debug("[Recommendation] 필터링된 메뉴 조회 완료 - {}건", menus.size());
		return menus;
	}

	@Override
	public List<MenuWithBeanScoreDto> findMenusByBeanId(Long beanId, GeoUtils.BoundingBox boundingBox) {
		BigDecimal minLat = null, maxLat = null, minLon = null, maxLon = null;
		if (boundingBox != null) {
			minLat = boundingBox.minLatitude();
			maxLat = boundingBox.maxLatitude();
			minLon = boundingBox.minLongitude();
			maxLon = boundingBox.maxLongitude();
		}

		List<MenuWithBeanScoreDto> menus = recommendationQueryMapper.findMenusByBeanId(
			beanId, minLat, maxLat, minLon, maxLon);

		log.debug("[Recommendation] 원두 사용 메뉴 조회 완료 - beanId: {}, {}건", beanId, menus.size());
		return menus;
	}

	@Override
	public Optional<Double> calculateDistance(MenuWithBeanScoreDto menu, BigDecimal latitude, BigDecimal longitude) {
		if (menu.storeLatitude() == null || menu.storeLongitude() == null
			|| latitude == null || longitude == null) {
			return Optional.empty();
		}

		return Optional.of(GeoUtils.calculateHaversineDistance(
			latitude.doubleValue(),
			longitude.doubleValue(),
			menu.storeLatitude().doubleValue(),
			menu.storeLongitude().doubleValue()));
	}
}
