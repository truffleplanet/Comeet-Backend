package com.backend.domain.ai.scheduler;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.backend.domain.ai.service.batch.BatchImageGenerationFacadeService;
import com.backend.domain.passport.service.facade.PassportFacadeService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class PassportImageScheduler {

	private final BatchImageGenerationFacadeService batchImageGenerationFacadeService;
	private final PassportFacadeService passportFacadeService;

	@Scheduled(cron = "0 0 2 1 * *", zone = "UTC")
	public void generateMonthlyPassportImages() {
		LocalDateTime now = LocalDateTime.now();
		YearMonth lastMonth = YearMonth.from(now.minusMonths(1));
		int year = lastMonth.getYear();
		int month = lastMonth.getMonthValue();

		log.info("[Scheduler] 월별 Passport 이미지 자동 생성 시작 - year: {}, month: {}", year, month);

		try {
			List<Long> userIds = passportFacadeService.findUsersWithVisitsInMonth(year, month);

			if (userIds.isEmpty()) {
				log.warn("[Scheduler] 전월 방문 기록이 있는 사용자 없음 - year: {}, month: {}", year, month);
				return;
			}

			String batchId = batchImageGenerationFacadeService.generatePassportImagesForAllUsers(userIds).batchId();

			log.info("[Scheduler] 월별 Passport 이미지 자동 생성 완료 - batchId: {}, 사용자 수: {}, year: {}, month: {}",
				batchId, userIds.size(), year, month);

		} catch (RuntimeException e) {
			log.error("[Scheduler] 월별 Passport 이미지 자동 생성 실패 - year: {}, month: {}", year, month, e);
		}
	}
}
