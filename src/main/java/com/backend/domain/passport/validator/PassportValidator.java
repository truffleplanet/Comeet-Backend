package com.backend.domain.passport.validator;

import java.time.LocalDate;
import java.time.Year;

import org.springframework.stereotype.Component;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.passport.entity.Passport;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class PassportValidator {

	private static final int MIN_YEAR = 2020;

	public void validateYear(final Integer year) {
		int currentYear = Year.now().getValue();
		if (year < MIN_YEAR || year > currentYear) {
			log.warn("[Passport] 잘못된 연도: {}", year);
			throw new BusinessException(ErrorCode.INVALID_YEAR);
		}
	}

	public void validateAvailability(final Passport passport) {
		if (!isAvailable(passport.getYear(), passport.getMonth())) {
			log.warn("[Passport] 아직 여권을 열람할 수 없습니다: {}-{}", passport.getYear(), passport.getMonth());
			throw new BusinessException(ErrorCode.PASSPORT_NOT_AVAILABLE_YET);
		}
	}

	public void validateOwnership(final Passport passport, final Long userId) {
		if (!passport.getUserId().equals(userId)) {
			log.warn("[Passport] 액세스 거부됨: 사용자 {}가 {} 사용자 소유의 여권에 액세스하려고 했습니다.",
				userId, passport.getUserId());
			throw new BusinessException(ErrorCode.PASSPORT_ACCESS_DENIED);
		}
	}

	public boolean isAvailable(final Integer year, final Integer month) {
		LocalDate passportMonth = LocalDate.of(year, month, 1);
		LocalDate availableFrom = passportMonth.plusMonths(1);
		LocalDate today = LocalDate.now();
		return !today.isBefore(availableFrom);
	}
}
