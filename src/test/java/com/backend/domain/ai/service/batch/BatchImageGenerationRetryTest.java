package com.backend.domain.ai.service.batch;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.ai.entity.BatchProgress;
import com.backend.domain.ai.repository.BatchProgressRepository;
import com.backend.domain.ai.service.ImageGenerationService;
import com.backend.domain.passport.entity.Passport;
import com.backend.domain.passport.service.query.PassportQueryService;

@SpringBootTest(properties = "app.retry.batch-image.delay=10")
@ActiveProfiles("local")
@DisplayName("Batch image generation retry test")
class BatchImageGenerationRetryTest {

	@Autowired
	private BatchImageGenerationFacadeService facadeService;

	@MockitoBean
	private ImageGenerationService imageGenerationService;

	@MockitoBean
	private PassportQueryService passportQueryService;

	@MockitoBean
	private BatchProgressRepository progressRepository;

	@Test
	@DisplayName("image generation retries transient failures before publishing completion event")
	void generatePassportImages_RetriesTransientImageGenerationFailure() {
		Passport passport = Passport.builder()
			.id(1L)
			.userId(1L)
			.originSequence("Ethiopia,Colombia")
			.build();
		when(passportQueryService.findLatestByUserId(1L)).thenReturn(passport);

		AtomicInteger attempts = new AtomicInteger();
		when(imageGenerationService.generate(anyString()))
			.thenAnswer(invocation -> {
				if (attempts.incrementAndGet() < 3) {
					throw new BusinessException(ErrorCode.AI_IMAGE_GENERATION_FAILED);
				}
				return "https://example.com/generated-passport.png";
			});

		facadeService.generatePassportImagesForAllUsers(List.of(1L));

		verify(imageGenerationService, timeout(3_000).times(3)).generate(anyString());
		verify(progressRepository, timeout(1_000)).update(org.mockito.ArgumentMatchers.any(BatchProgress.class));
	}
}
