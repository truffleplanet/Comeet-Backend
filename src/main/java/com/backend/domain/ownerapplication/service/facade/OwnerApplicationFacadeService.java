package com.backend.domain.ownerapplication.service.facade;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.ownerapplication.dto.request.OwnerApplicationCreateReqDto;
import com.backend.domain.ownerapplication.dto.request.OwnerApplicationRejectReqDto;
import com.backend.domain.ownerapplication.dto.response.OwnerApplicationResDto;
import com.backend.domain.ownerapplication.entity.OwnerApplication;
import com.backend.domain.ownerapplication.entity.OwnerApplicationStatus;
import com.backend.domain.ownerapplication.service.command.OwnerApplicationCommandService;
import com.backend.domain.ownerapplication.service.query.OwnerApplicationQueryService;
import com.backend.domain.store.service.facade.StoreFacadeService;
import com.backend.domain.user.dto.response.UserInfoResDto;
import com.backend.domain.user.entity.Role;
import com.backend.domain.user.service.command.UserCommandService;
import com.backend.domain.user.service.query.UserQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class OwnerApplicationFacadeService {

	private final OwnerApplicationCommandService commandService;
	private final OwnerApplicationQueryService queryService;
	private final StoreFacadeService storeFacadeService;
	private final UserCommandService userCommandService;
	private final UserQueryService userQueryService;

	public OwnerApplicationResDto apply(final Long userId, final OwnerApplicationCreateReqDto reqDto) {
		validateApplicant(userId);

		if (queryService.existsPendingByUserId(userId)) {
			throw new BusinessException(ErrorCode.OWNER_APPLICATION_ALREADY_PENDING);
		}

		OwnerApplication application = OwnerApplication.create(userId, reqDto);
		return OwnerApplicationResDto.from(commandService.save(application));
	}

	public OwnerApplicationResDto approve(final Long applicationId, final Long adminId) {
		OwnerApplication application = queryService.findById(applicationId);
		validatePending(application);

		commandService.approve(applicationId, adminId);
		storeFacadeService.createStore(application.toStoreCreateReqDto(), application.getUserId());
		userCommandService.updateRole(application.getUserId(), Role.OWNER);

		return OwnerApplicationResDto.from(queryService.findById(applicationId));
	}

	public OwnerApplicationResDto reject(
		final Long applicationId,
		final Long adminId,
		final OwnerApplicationRejectReqDto reqDto
	) {
		OwnerApplication application = queryService.findById(applicationId);
		validatePending(application);

		commandService.reject(applicationId, adminId, reqDto.rejectReason());
		return OwnerApplicationResDto.from(queryService.findById(applicationId));
	}

	@Transactional(readOnly = true)
	public OwnerApplicationResDto findById(final Long applicationId) {
		return OwnerApplicationResDto.from(queryService.findById(applicationId));
	}

	@Transactional(readOnly = true)
	public OwnerApplicationResDto findLatestByUserId(final Long userId) {
		return OwnerApplicationResDto.from(queryService.findLatestByUserId(userId));
	}

	@Transactional(readOnly = true)
	public List<OwnerApplicationResDto> findAllByStatus(final OwnerApplicationStatus status) {
		return queryService.findAllByStatus(status)
			.stream()
			.map(OwnerApplicationResDto::from)
			.toList();
	}

	private void validateApplicant(final Long userId) {
		UserInfoResDto user = userQueryService.findById(userId);
		if (user.role() != Role.USER) {
			throw new BusinessException(ErrorCode.OWNER_APPLICATION_NOT_ALLOWED);
		}
	}

	private void validatePending(final OwnerApplication application) {
		if (application.getStatus() != OwnerApplicationStatus.PENDING) {
			throw new BusinessException(ErrorCode.OWNER_APPLICATION_NOT_PENDING);
		}
	}
}
