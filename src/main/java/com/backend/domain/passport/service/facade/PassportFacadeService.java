package com.backend.domain.passport.service.facade;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.backend.domain.passport.converter.PassportConverter;
import com.backend.domain.passport.converter.StatisticsConverter;
import com.backend.domain.passport.dto.common.CountryStatDto;
import com.backend.domain.passport.dto.common.PassportRecordDto;
import com.backend.domain.passport.dto.common.PassportSummaryDto;
import com.backend.domain.passport.dto.common.RoasteryStatDto;
import com.backend.domain.passport.dto.response.CountryStatisticsResDto;
import com.backend.domain.passport.dto.response.PassportDetailResDto;
import com.backend.domain.passport.dto.response.PassportListResDto;
import com.backend.domain.passport.dto.response.RoasteryStatisticsResDto;
import com.backend.domain.passport.entity.Passport;
import com.backend.domain.passport.service.calculator.PassportStatisticsCalculator;
import com.backend.domain.passport.service.command.PassportCommandService;
import com.backend.domain.passport.service.query.PassportQueryService;
import com.backend.domain.passport.service.query.PassportRecordQueryService;
import com.backend.domain.passport.service.query.PassportStatisticsQueryService;
import com.backend.domain.passport.validator.PassportValidator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class PassportFacadeService {

	private final PassportQueryService passportQueryService;
	private final PassportCommandService passportCommandService;
	private final PassportRecordQueryService passportRecordQueryService;
	private final PassportStatisticsQueryService passportStatisticsQueryService;

	private final PassportStatisticsCalculator statisticsCalculator;
	private final PassportValidator passportValidator;

	public PassportListResDto getPassportList(final Long userId, final Integer year) {
		passportValidator.validateYear(year);
		List<Passport> passports = passportQueryService.findAllByUserIdAndYear(userId, year);

		List<PassportSummaryDto> summaries = passports.stream()
			.map(passport -> {
				boolean isAvailable = passportValidator.isAvailable(passport.getYear(), passport.getMonth());
				return PassportConverter.toPassportSummaryDto(passport, isAvailable);
			})
			.toList();

		return PassportConverter.toPassportListResDto(year, summaries);
	}

	public PassportDetailResDto getPassportDetail(final Long passportId, final Long userId) {
		Passport passport = passportQueryService.findById(passportId);
		passportValidator.validateOwnership(passport, userId);
		passportValidator.validateAvailability(passport);

		List<PassportRecordDto> records = passportRecordQueryService.findRecordsByPassportId(passportId);
		return PassportConverter.toPassportDetailResDto(passport, records);
	}

	public RoasteryStatisticsResDto getRoasteryStatistics(final Long userId) {
		List<RoasteryStatDto> statistics = passportStatisticsQueryService.getRoasteryStatistics(userId);
		return StatisticsConverter.toRoasteryStatDto(statistics);
	}

	public CountryStatisticsResDto getCountryStatistics(final Long userId) {
		List<CountryStatDto> statistics = passportStatisticsQueryService.getCountryStatistics(userId);
		return StatisticsConverter.toCountryStatisticsResDto(statistics);
	}

	public List<Long> findUsersWithVisitsInMonth(final int year, final int month) {
		return passportQueryService.findUsersWithVisitsInMonth(year, month);
	}

	@Transactional
	public void generatePassportForUser(Long userId, int year, int month) {
		Optional<Passport> existingPassport = passportQueryService.findByUserIdAndYearAndMonth(userId, year, month);
		if (existingPassport.isPresent()) {
			log.warn("[Passport] 여권 이미 존재, userId={}, year={}, month={}", userId, year, month);
			return;
		}

		List<Map<String, Object>> visits = passportQueryService.findVisitsWithMenuInMonth(userId, year, month);
		if (CollectionUtils.isEmpty(visits)) {
			return;
		}

		PassportStatisticsCalculator.PassportStatistics stats = statisticsCalculator.calculate(visits);

		Passport passport = PassportConverter.toPassport(userId, year, month, stats);
		Long passportId = passportCommandService.createPassport(passport);

		for (Map<String, Object> visit : visits) {
			Long visitId = (Long)visit.get("visit_id");
			passportCommandService.addPassportVisit(passportId, visitId);
		}

		log.info("[Passport] 여권 생성 완료, passportId={}, userId={}", passportId, userId);
	}

	@Transactional
	public void generatePassportsForMonth(int year, int month, Long userId) {
		log.info("[Passport] 여권 생성 테스트 시작 - year={}, month={}, userId={}", year, month, userId);

		if (userId != null) {
			generatePassportForUser(userId, year, month);
			log.info("[Passport] 여권 생성 테스트 완료 - userId={}", userId);
		} else {
			List<Long> userIds = findUsersWithVisitsInMonth(year, month);
			log.info("[Passport] 대상 사용자 조회 완료 - count={}", userIds.size());

			int successCount = 0;
			int failCount = 0;

			for (Long targetUserId : userIds) {
				try {
					generatePassportForUser(targetUserId, year, month);
					successCount++;
				} catch (RuntimeException e) {
					log.error("[Passport] 여권 생성 실패 - userId={}", targetUserId, e);
					failCount++;
				}
			}

			log.info("[Passport] 여권 생성 테스트 완료 - success={}, failed={}", successCount, failCount);
		}
	}

}
