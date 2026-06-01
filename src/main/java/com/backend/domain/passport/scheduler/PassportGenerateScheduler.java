package com.backend.domain.passport.scheduler;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.passport.service.facade.PassportFacadeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PassportGenerateScheduler {

	private final PassportFacadeService passportFacadeService;

	//? 비동기 작업으로 작업을 효율적으로 처리하는 것이 가능할 것 같으니, 추후 검토가 필요
	@Scheduled(cron = "0 0 0 1 * ?")
	public void generateMonthlyPassports() {
		LocalDate now = LocalDate.now();
		YearMonth previousMonth = YearMonth.from(now.minusMonths(1));
		int year = previousMonth.getYear();
		int month = previousMonth.getMonthValue();

		log.info("[Passport] 월간 여권 생성 시작, year={}, month={}", year, month);

		try {
			List<Long> userIds = passportFacadeService.findUsersWithVisitsInMonth(year, month);
			log.info("[Passport] 대상 사용자 조회 완료, count={}", userIds.size());

			int successCount = 0;
			int failCount = 0;

			for (Long userId : userIds) {
				try {
					passportFacadeService.generatePassportForUser(userId, year, month);
					successCount++;
				} catch (Exception e) {
					log.error("[Passport] 여권 생성 실패, userId={}", userId, e);
					failCount++;
				}
			}

			log.info("[Passport] 월간 여권 생성 완료, success={}, failed={}", successCount, failCount);

		} catch (Exception e) {
			log.error("[Passport] 월간 여권 생성 중 오류 발생", e);
			throw new BusinessException(ErrorCode.PASSPORT_GENERATION_FAILED);
		}
	}
}
