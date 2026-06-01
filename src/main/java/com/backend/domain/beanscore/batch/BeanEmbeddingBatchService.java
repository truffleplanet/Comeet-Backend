package com.backend.domain.beanscore.batch;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.ai.service.EmbeddingService;
import com.backend.common.ai.util.EmbeddingTextBuilder;
import com.backend.common.redis.service.RedisVectorService;
import com.backend.domain.bean.service.query.BeanFlavorQueryService;
import com.backend.domain.beanscore.entity.BeanScore;
import com.backend.domain.beanscore.service.query.BeanScoreQueryService;
import com.backend.domain.flavor.entity.Flavor;
import com.backend.domain.flavor.service.FlavorQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 원두 임베딩 배치 서비스
 * <p>
 * bean_flavor_notes의 flavor를 임베딩하여 Redis Vector에 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeanEmbeddingBatchService {

	private final BeanScoreQueryService beanScoreQueryService;
	private final BeanFlavorQueryService beanFlavorQueryService;
	private final FlavorQueryService flavorQueryService;
	private final EmbeddingService embeddingService;
	private final RedisVectorService redisVectorService;

	private static final int BATCH_SIZE = 50;

	/**
	 * 모든 원두 임베딩 생성 및 저장 (flavor가 있는 원두만)
	 *
	 * @return 처리된 원두 수
	 */
	@Transactional(readOnly = true)
	public int embedAllBeans() {
		log.info("[Embedding] 원두 임베딩 배치 시작 (flavor 있는 원두만)");

		List<BeanScore> allBeanScores = beanScoreQueryService.findAll();
		log.info("[Embedding] 처리 대상 BeanScore 조회 완료 - {}건", allBeanScores.size());

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger skipCount = new AtomicInteger(0);
		AtomicInteger failCount = new AtomicInteger(0);

		// 배치 처리
		for (int i = 0; i < allBeanScores.size(); i += BATCH_SIZE) {
			int end = Math.min(i + BATCH_SIZE, allBeanScores.size());
			List<BeanScore> batch = allBeanScores.subList(i, end);

			processBatch(batch, successCount, skipCount, failCount);

			log.info("[Embedding] 배치 처리 진행 - {}/{}", end, allBeanScores.size());
		}

		log.info("[Embedding] 배치 완료 - 성공: {}건, 스킵: {}건, 실패: {}건",
			successCount.get(), skipCount.get(), failCount.get());

		return successCount.get();
	}

	/**
	 * 기존 임베딩 전체 삭제 후 재생성 (flavor가 있는 원두만)
	 *
	 * @return 결과 정보
	 */
	@Transactional(readOnly = true)
	public EmbedResult dropAndEmbedAll() {
		log.info("[Embedding] 전체 임베딩 삭제 후 재생성 시작");

		// 1. 기존 임베딩 전체 삭제
		int deletedCount = redisVectorService.dropAllEmbeddings();
		log.info("[Embedding] 기존 임베딩 삭제 완료 - {}건", deletedCount);

		// 2. flavor가 있는 원두만 임베딩
		int embeddedCount = embedAllBeans();

		return new EmbedResult(deletedCount, embeddedCount);
	}

	public record EmbedResult(int deletedCount, int embeddedCount) {
	}

	/**
	 * 배치 처리 (flavor가 있는 원두만 임베딩)
	 */
	private void processBatch(
		List<BeanScore> batch, AtomicInteger successCount, AtomicInteger skipCount,
		AtomicInteger failCount
	) {
		for (BeanScore beanScore : batch) {
			try {
				// bean_flavor_notes에서 flavor 조회
				List<Flavor> flavors = beanFlavorQueryService.findFlavorsByBeanId(beanScore.getBeanId());

				// flavor가 없으면 임베딩 skip
				if (flavors == null || flavors.isEmpty()) {
					log.debug("[Embedding] 원두 스킵 (flavor 없음) - beanId: {}", beanScore.getBeanId());
					skipCount.incrementAndGet();
					continue;
				}

				// 플레이버 태그를 계층 구조 경로로 변환
				List<String> flavorCodes = flavors.stream()
					.map(Flavor::getCode)
					.toList();
				List<String> hierarchyPaths = flavorQueryService.getHierarchyPaths(flavorCodes);

				// 점수 + 플레이버를 조합한 임베딩 텍스트 생성
				String embeddingText = EmbeddingTextBuilder.buildBeanEmbeddingText(
					beanScore.getAcidity(),
					beanScore.getBody(),
					beanScore.getSweetness(),
					beanScore.getBitterness(),
					beanScore.getRoastLevel(),
					hierarchyPaths
				);
				log.debug("[Embedding] 임베딩 텍스트 생성 - beanId: {}, text: {}", beanScore.getBeanId(), embeddingText);

				float[] embedding = embeddingService.embed(embeddingText);
				redisVectorService.saveEmbedding(beanScore.getBeanId(), embedding);
				successCount.incrementAndGet();

				log.debug("[Embedding] 임베딩 저장 완료 - beanId: {}", beanScore.getBeanId());
			} catch (RuntimeException e) {
				log.error("[Embedding] 임베딩 실패 - beanId: {}", beanScore.getBeanId(), e);
				failCount.incrementAndGet();
			}
		}
	}

	/**
	 * 특정 원두 임베딩 업데이트 (flavor가 없으면 삭제)
	 *
	 * @param beanId 원두 ID
	 */
	public void updateEmbedding(Long beanId) {
		try {
			// BeanScore 조회
			BeanScore beanScore = beanScoreQueryService.findByBeanId(beanId).orElse(null);
			if (beanScore == null) {
				log.debug("[Embedding] 업데이트 스킵 (BeanScore 없음) - beanId: {}", beanId);
				redisVectorService.deleteEmbedding(beanId);
				return;
			}

			// bean_flavor_notes에서 flavor 조회
			List<Flavor> flavors = beanFlavorQueryService.findFlavorsByBeanId(beanId);

			// flavor가 없으면 기존 임베딩 삭제
			if (flavors == null || flavors.isEmpty()) {
				log.debug("[Embedding] 임베딩 삭제 (flavor 없음) - beanId: {}", beanId);
				redisVectorService.deleteEmbedding(beanId);
				return;
			}

			// 플레이버 태그를 계층 구조 경로로 변환
			List<String> flavorCodes = flavors.stream()
				.map(Flavor::getCode)
				.toList();
			List<String> hierarchyPaths = flavorQueryService.getHierarchyPaths(flavorCodes);

			// 점수 + 플레이버를 조합한 임베딩 텍스트 생성
			String embeddingText = EmbeddingTextBuilder.buildBeanEmbeddingText(
				beanScore.getAcidity(),
				beanScore.getBody(),
				beanScore.getSweetness(),
				beanScore.getBitterness(),
				beanScore.getRoastLevel(),
				hierarchyPaths
			);
			log.debug("[Embedding] 임베딩 텍스트 생성 - beanId: {}, text: {}", beanId, embeddingText);

			float[] embedding = embeddingService.embed(embeddingText);
			redisVectorService.saveEmbedding(beanId, embedding);
			log.debug("[Embedding] 임베딩 업데이트 완료 - beanId: {}", beanId);
		} catch (RuntimeException e) {
			log.error("[Embedding] 임베딩 업데이트 실패 - beanId: {}", beanId, e);
		}
	}

	/**
	 * 특정 원두 임베딩 삭제
	 *
	 * @param beanId 원두 ID
	 */
	public void deleteEmbedding(Long beanId) {
		try {
			redisVectorService.deleteEmbedding(beanId);
			log.debug("[Embedding] 임베딩 삭제 완료 - beanId: {}", beanId);
		} catch (RuntimeException e) {
			log.error("[Embedding] 임베딩 삭제 실패 - beanId: {}", beanId, e);
		}
	}

	/**
	 * 임베딩이 없는 원두만 처리 (flavor가 있는 원두만)
	 *
	 * @return 처리된 원두 수
	 */
	@Transactional(readOnly = true)
	public int embedMissingBeans() {
		log.info("[Embedding] 누락 임베딩 처리 시작 (flavor 있는 원두만)");

		List<BeanScore> allBeanScores = beanScoreQueryService.findAll();
		AtomicInteger processedCount = new AtomicInteger(0);
		AtomicInteger skipCount = new AtomicInteger(0);

		for (BeanScore beanScore : allBeanScores) {
			if (!redisVectorService.existsEmbedding(beanScore.getBeanId())) {
				try {
					// bean_flavor_notes에서 flavor 조회
					List<Flavor> flavors = beanFlavorQueryService.findFlavorsByBeanId(beanScore.getBeanId());

					// flavor가 없으면 skip
					if (flavors == null || flavors.isEmpty()) {
						log.debug("[Embedding] 원두 스킵 (flavor 없음) - beanId: {}", beanScore.getBeanId());
						skipCount.incrementAndGet();
						continue;
					}

					// 플레이버 태그를 계층 구조 경로로 변환
					List<String> flavorCodes = flavors.stream()
						.map(Flavor::getCode)
						.toList();
					List<String> hierarchyPaths = flavorQueryService.getHierarchyPaths(flavorCodes);

					// 점수 + 플레이버를 조합한 임베딩 텍스트 생성
					String embeddingText = EmbeddingTextBuilder.buildBeanEmbeddingText(
						beanScore.getAcidity(),
						beanScore.getBody(),
						beanScore.getSweetness(),
						beanScore.getBitterness(),
						beanScore.getRoastLevel(),
						hierarchyPaths
					);
					log.debug("[Embedding] 임베딩 텍스트 생성 - beanId: {}, text: {}", beanScore.getBeanId(), embeddingText);

					float[] embedding = embeddingService.embed(embeddingText);
					redisVectorService.saveEmbedding(beanScore.getBeanId(), embedding);
					processedCount.incrementAndGet();
					log.debug("[Embedding] 누락 임베딩 생성 완료 - beanId: {}", beanScore.getBeanId());
				} catch (RuntimeException e) {
					log.error("[Embedding] 누락 임베딩 생성 실패 - beanId: {}", beanScore.getBeanId(), e);
				}
			}
		}

		log.info("[Embedding] 누락 임베딩 처리 완료 - 처리: {}건, 스킵: {}건",
			processedCount.get(), skipCount.get());
		return processedCount.get();
	}
}
