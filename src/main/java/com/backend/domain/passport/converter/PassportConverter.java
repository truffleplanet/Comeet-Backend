package com.backend.domain.passport.converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.backend.domain.passport.dto.common.PassportInfoDto;
import com.backend.domain.passport.dto.common.PassportRecordDto;
import com.backend.domain.passport.dto.common.PassportSummaryDto;
import com.backend.domain.passport.dto.response.PassportDetailResDto;
import com.backend.domain.passport.dto.response.PassportListResDto;
import com.backend.domain.passport.entity.Passport;
import com.backend.domain.passport.service.calculator.PassportStatisticsCalculator.PassportStatistics;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PassportConverter {

	public Passport toPassport(final Long userId, final int year, final int month, final PassportStatistics stats) {
		return Passport.builder()
			.userId(userId)
			.year(year)
			.month(month)
			.coverImageUrl(null) // 비동기 AI 이미지 생성 이벤트 처리 전 초기값
			.totalCoffeeCount(stats.totalCoffeeCount())
			.totalStoreCount(stats.totalStoreCount())
			.totalBeanCount(stats.totalBeanCount())
			.topOrigin(stats.topOrigin())
			.topRoastery(stats.topRoastery())
			.originSequence(stats.originSequence())
			.totalOriginDistance(stats.totalOriginDistance())
			.build();
	}

	public PassportSummaryDto toPassportSummaryDto(final Passport passport, final boolean isAvailable) {
		return PassportSummaryDto.builder()
			.passportId(passport.getId())
			.month(passport.getMonth())
			.coverImageUrl(passport.getCoverImageUrl())
			.totalCoffeeCount(passport.getTotalCoffeeCount())
			.totalStoreCount(passport.getTotalStoreCount())
			.topOrigin(passport.getTopOrigin())
			.isAvailable(isAvailable)
			.build();
	}

	public PassportListResDto toPassportListResDto(final Integer year, final List<PassportSummaryDto> summaries) {
		return PassportListResDto.builder()
			.year(year)
			.passports(summaries)
			.build();
	}

	public PassportInfoDto toPassportInfoDto(final Passport passport) {
		List<String> originSequence = parseOriginSequence(passport.getOriginSequence());

		return PassportInfoDto.builder()
			.totalCoffeeCount(passport.getTotalCoffeeCount())
			.totalStoreCount(passport.getTotalStoreCount())
			.totalBeanCount(passport.getTotalBeanCount())
			.topOrigin(passport.getTopOrigin())
			.topRoastery(passport.getTopRoastery())
			.originSequence(originSequence)
			.totalOriginDistance(passport.getTotalOriginDistance())
			.build();
	}

	public PassportDetailResDto toPassportDetailResDto(final Passport passport, final List<PassportRecordDto> records) {
		return PassportDetailResDto.builder()
			.passportId(passport.getId())
			.year(passport.getYear())
			.month(passport.getMonth())
			.coverImageUrl(passport.getCoverImageUrl())
			.info(toPassportInfoDto(passport))
			.records(records)
			.build();
	}

	private List<String> parseOriginSequence(final String originSequence) {
		if (originSequence == null || originSequence.trim().isEmpty()) {
			return Collections.emptyList();
		}
		return Arrays.asList(originSequence.split(","));
	}
}
