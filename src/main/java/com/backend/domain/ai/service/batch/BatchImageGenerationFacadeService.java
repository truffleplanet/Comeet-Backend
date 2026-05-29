package com.backend.domain.ai.service.batch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.ai.converter.BatchConverter;
import com.backend.domain.ai.dto.response.BatchImageGenerationResDto;
import com.backend.domain.ai.dto.response.BatchProgressResDto;
import com.backend.domain.ai.entity.BatchProgress;
import com.backend.domain.ai.event.PassportImageGeneratedEvent;
import com.backend.domain.ai.repository.BatchProgressRepository;
import com.backend.domain.ai.service.ImageGenerationService;
import com.backend.domain.passport.entity.Passport;
import com.backend.domain.passport.service.query.PassportQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchImageGenerationFacadeService {

	private static final int PERMITS = 50;

	private final ImageGenerationService imageGenerationService;
	private final PassportQueryService passportQueryService;
	private final BatchProgressRepository progressRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final Executor virtualThreadExecutor;

	@Value("classpath:prompts/passport_image_prompt.txt")
	private Resource passportImagePrompt;

	private final Semaphore semaphore = new Semaphore(PERMITS);

	public BatchImageGenerationResDto generatePassportImagesForAllUsers(final List<Long> userIds) {
		String batchId = UUID.randomUUID().toString();
		int totalUsers = userIds.size();
		log.info("[Batch Image] 배치 작업 시작 - batchId: {}, 총 사용자 수: {}", UUID.randomUUID(), userIds.size());

		BatchProgress progress = BatchProgress.init(batchId, totalUsers);
		progressRepository.save(progress);

		userIds.forEach(userId -> generateImageAsync(batchId, userId, progress));
		return BatchConverter.toBatchImageGenerationResDto(batchId, userIds.size());
	}

	public BatchImageGenerationResDto generateMonthlyBatchImages(final Integer year, final Integer month) {
		List<Long> userIds = passportQueryService.findUsersWithVisitsInMonth(year, month);

		if (userIds.isEmpty()) {
			log.warn("[Batch API] 해당 월에 방문 기록이 있는 사용자 없음 - year: {}, month: {}", year, month);
			return BatchConverter.toBatchImageGenerationResDto("NO_USERS", 0);
		}

		return generatePassportImagesForAllUsers(userIds);
	}

	public BatchProgressResDto getProgress(final String batchId) {
		BatchProgress batchProgress = progressRepository.findById(batchId);
		return BatchConverter.toBatchProgressResDto(batchProgress);
	}

	private void generateImageAsync(final String batchId, final Long userId, final BatchProgress progress) {
		CompletableFuture.runAsync(() -> {
			try {
				semaphore.acquire();
				log.debug("[Batch Image] 작업 시작 - userId: {}, 활성 작업 수: {}",
					userId, PERMITS - semaphore.availablePermits());

				try {
					processUserImage(batchId, userId);
					progress.incrementCompleted();
					log.info("[Batch Image] 작업 완료 - userId: {}, 진행률: {}/{} ,{} %",
						userId, progress.getCompleted().get(), progress.getTotal(), progress.getProgressPercentage());
				} finally {
					semaphore.release();
					updateProgressInRedis(progress);
				}

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				log.error("[Batch Image] 작업 중단 - userId: {}", userId, e);
				progress.incrementFailed();
				updateProgressInRedis(progress);
			} catch (Exception e) {
				log.error("[Batch Image] 작업 실패 - userId: {}", userId, e);
				progress.incrementFailed();
				updateProgressInRedis(progress);
			}
		}, virtualThreadExecutor);
	}

	@Retryable(
		retryFor = {BusinessException.class},
		backoff = @Backoff(delay = 5000)
	)
	private void processUserImage(final String batchId, final Long userId) {
		try {
			Passport passport = passportQueryService.findLatestByUserId(userId);
			String prompt = buildPromptFromPassport(passport);
			log.debug("[Batch Image] 이미지 생성 시작 - userId: {}, passportId: {}", userId, passport.getId());
			String imageUrl = imageGenerationService.generate(prompt);

			eventPublisher.publishEvent(
				new PassportImageGeneratedEvent(this, batchId, userId, passport.getId(), imageUrl));

			log.info("[Batch Image] 이미지 생성 성공 - userId: {}, passportId: {}, imageUrl: {}",
				userId, passport.getId(), imageUrl);

		} catch (BusinessException e) {
			log.error("[Batch Image] AI 이미지 생성 실패 - userId: {}", userId, e);
			throw e;
		} catch (Exception e) {
			log.error("[Batch Image] 예상치 못한 오류 - userId: {}", userId, e);
			throw new BusinessException(ErrorCode.AI_IMAGE_GENERATION_FAILED);
		}
	}

	private String buildPromptFromPassport(final Passport passport) {
		try {
			return String.format(passportImagePrompt.getContentAsString(StandardCharsets.UTF_8),
				passport.getOriginSequence());
		} catch (IOException e) {
			throw new BusinessException(ErrorCode.USER_PROMPT_REQUIRED);
		}
	}

	private void updateProgressInRedis(final BatchProgress progress) {
		if (progress.isCompleted()) {
			progress.markAsCompleted();
			log.info("[Batch Image] 배치 작업 완료 - batchId: {}, 성공: {}, 실패: {}",
				progress.getBatchId(), progress.getCompleted().get(), progress.getFailed().get());
		}
		progressRepository.update(progress);
	}
}
