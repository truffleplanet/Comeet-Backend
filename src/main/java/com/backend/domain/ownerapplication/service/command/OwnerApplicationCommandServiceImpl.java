package com.backend.domain.ownerapplication.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.ownerapplication.entity.OwnerApplication;
import com.backend.domain.ownerapplication.entity.OwnerApplicationStatus;
import com.backend.domain.ownerapplication.mapper.command.OwnerApplicationCommandMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OwnerApplicationCommandServiceImpl implements OwnerApplicationCommandService {

	private final OwnerApplicationCommandMapper commandMapper;

	@Override
	public OwnerApplication save(final OwnerApplication application) {
		commandMapper.save(application);
		log.info("[OwnerApplication] 신청 저장 완료 - id: {}, userId: {}", application.getId(), application.getUserId());
		return application;
	}

	@Override
	public void approve(final Long applicationId, final Long adminId) {
		int updated = commandMapper.approve(applicationId, adminId);
		if (updated == 0) {
			throw new BusinessException(ErrorCode.OWNER_APPLICATION_NOT_PENDING);
		}
		log.info("[OwnerApplication] 신청 승인 완료 - id: {}, adminId: {}", applicationId, adminId);
	}

	@Override
	public void reject(final Long applicationId, final Long adminId, final String rejectReason) {
		int updated = commandMapper.reject(applicationId, adminId, rejectReason);
		if (updated == 0) {
			throw new BusinessException(ErrorCode.OWNER_APPLICATION_NOT_PENDING);
		}
		log.info("[OwnerApplication] 신청 거절 완료 - id: {}, adminId: {}", applicationId, adminId);
	}

	@Override
	public void saveReviewHistory(
		final Long applicationId,
		final Long reviewerId,
		final OwnerApplicationStatus status,
		final String comment
	) {
		commandMapper.saveReviewHistory(applicationId, reviewerId, status, comment);
		log.info(
			"[OwnerApplication] 검토 이력 저장 완료 - applicationId: {}, reviewerId: {}, status: {}",
			applicationId,
			reviewerId,
			status
		);
	}
}
