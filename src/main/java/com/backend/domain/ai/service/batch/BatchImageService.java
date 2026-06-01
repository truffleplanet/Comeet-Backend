package com.backend.domain.ai.service.batch;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.backend.domain.ai.service.ImageGenerationService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchImageService {

	private final ImageGenerationService imageGenerationService;

	@Retryable(
		retryFor = {Exception.class},
		maxAttempts = 3,
		backoff = @Backoff(delayExpression = "${app.retry.batch-image.delay:5000}")
	)
	public String generateImageWithRetry(final String prompt) {
		log.info("[Batch Image] 이미지 생성 API 호출");
		return imageGenerationService.generate(prompt);
	}
}
