package com.backend.domain.passport.service.query.implement;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.passport.entity.Passport;
import com.backend.domain.passport.mapper.query.PassportQueryMapper;
import com.backend.domain.passport.service.query.PassportQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class PassportQueryServiceImpl implements PassportQueryService {

	private final PassportQueryMapper passportQueryMapper;

	@Override
	public Passport findById(final Long passportId) {
		log.info("[Passport] 여권 조회 : passportId={}", passportId);
		Passport passport = passportQueryMapper.findById(passportId);
		if (passport == null) {
			throw new BusinessException(ErrorCode.PASSPORT_NOT_FOUND);
		}
		return passport;
	}

	@Override
	public List<Passport> findAllByUserIdAndYear(final Long userId, final Integer year) {
		log.info("[Passport] 여권 목록 조회 : userId={}, year={}", userId, year);
		return passportQueryMapper.findAllByUserIdAndYear(userId, year);
	}

	@Override
	public List<Long> findUsersWithVisitsInMonth(final int year, final int month) {
		log.info("[Passport] 월별 방문 유저 ID 목록 조회 : year={}, month={}", year, month);
		return passportQueryMapper.findUsersWithVisitsInMonth(year, month);
	}

	@Override
	public Optional<Passport> findByUserIdAndYearAndMonth(final Long userId, final int year, final int month) {
		log.info("[Passport] 유저 월별 여권 조회 : userId={}, year={}, month={}", userId, year, month);
		return passportQueryMapper.findByUserIdAndYearAndMonth(userId, year, month);
	}

	@Override
	public List<Map<String, Object>> findVisitsWithMenuInMonth(final Long userId, final int year, final int month) {
		log.info("[Passport] 유저 월별 방문 및 메뉴 상세 조회 : userId={}, year={}, month={}", userId, year, month);
		return passportQueryMapper.findVisitsWithMenuInMonth(userId, year, month);
	}

	@Override
	public Passport findLatestByUserId(final Long userId) {
		Passport passport = passportQueryMapper.findLatestByUserId(userId).
			orElseThrow(() -> new BusinessException(ErrorCode.PASSPORT_NOT_FOUND));
		log.info("[Passport] 최신 여권 조회 : userId={}", userId);
		return passport;
	}
}