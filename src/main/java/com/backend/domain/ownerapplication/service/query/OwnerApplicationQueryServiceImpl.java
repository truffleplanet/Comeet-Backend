package com.backend.domain.ownerapplication.service.query;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.ownerapplication.entity.OwnerApplication;
import com.backend.domain.ownerapplication.entity.OwnerApplicationStatus;
import com.backend.domain.ownerapplication.mapper.query.OwnerApplicationQueryMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OwnerApplicationQueryServiceImpl implements OwnerApplicationQueryService {

	private final OwnerApplicationQueryMapper queryMapper;

	@Override
	public OwnerApplication findById(final Long applicationId) {
		return queryMapper.findById(applicationId)
			.orElseThrow(() -> new BusinessException(ErrorCode.OWNER_APPLICATION_NOT_FOUND));
	}

	@Override
	public boolean existsPendingByUserId(final Long userId) {
		return queryMapper.countPendingByUserId(userId) > 0;
	}

	@Override
	public List<OwnerApplication> findAllByStatus(final OwnerApplicationStatus status) {
		List<OwnerApplication> applications = queryMapper.findAllByStatus(status);
		log.debug("[OwnerApplication] 신청 목록 조회 - status: {}, count: {}", status, applications.size());
		return applications;
	}
}
