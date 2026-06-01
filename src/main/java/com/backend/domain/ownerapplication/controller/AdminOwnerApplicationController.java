package com.backend.domain.ownerapplication.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.common.annotation.CurrentUser;
import com.backend.common.auth.constants.RoleAuthority;
import com.backend.common.auth.principal.AuthenticatedUser;
import com.backend.common.response.BaseResponse;
import com.backend.common.util.ResponseUtils;
import com.backend.domain.ownerapplication.dto.request.OwnerApplicationApproveReqDto;
import com.backend.domain.ownerapplication.dto.request.OwnerApplicationRejectReqDto;
import com.backend.domain.ownerapplication.dto.response.OwnerApplicationResDto;
import com.backend.domain.ownerapplication.dto.response.OwnerApplicationReviewHistoryResDto;
import com.backend.domain.ownerapplication.entity.OwnerApplicationStatus;
import com.backend.domain.ownerapplication.service.facade.OwnerApplicationFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin Owner Application", description = "관리자 가맹점주 신청 관리 API")
@RestController
@RequestMapping("/admin/owner-applications")
@PreAuthorize(RoleAuthority.ADMIN)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class AdminOwnerApplicationController {

	private final OwnerApplicationFacadeService ownerApplicationFacadeService;

	@Operation(
		summary = "가맹점주 신청 목록 조회",
		description = "관리자가 가맹점주 신청 목록을 상태별로 조회합니다. status를 생략하면 전체 조회합니다."
	)
	@GetMapping
	public ResponseEntity<BaseResponse<List<OwnerApplicationResDto>>> findAll(
		@RequestParam(required = false) OwnerApplicationStatus status
	) {
		return ResponseUtils.ok(ownerApplicationFacadeService.findAllByStatus(status));
	}

	@Operation(
		summary = "가맹점주 신청 상세 조회",
		description = "관리자가 특정 가맹점주 신청의 상세 정보와 검토 상태를 조회합니다."
	)
	@GetMapping("/{applicationId}")
	public ResponseEntity<BaseResponse<OwnerApplicationResDto>> findById(
		@PathVariable Long applicationId
	) {
		return ResponseUtils.ok(ownerApplicationFacadeService.findById(applicationId));
	}

	@Operation(
		summary = "가맹점주 신청 검토 이력 조회",
		description = "관리자가 특정 가맹점주 신청의 승인/거절 검토 이력을 조회합니다."
	)
	@GetMapping("/{applicationId}/histories")
	public ResponseEntity<BaseResponse<List<OwnerApplicationReviewHistoryResDto>>> findReviewHistories(
		@PathVariable Long applicationId
	) {
		return ResponseUtils.ok(ownerApplicationFacadeService.findReviewHistories(applicationId));
	}

	@Operation(
		summary = "가맹점주 신청 승인",
		description = "관리자가 신청을 승인합니다. 승인 시 가맹점이 생성되고 신청자는 OWNER로 승격됩니다."
	)
	@PostMapping("/{applicationId}/approve")
	public ResponseEntity<BaseResponse<OwnerApplicationResDto>> approve(
		@PathVariable Long applicationId,
		@CurrentUser AuthenticatedUser user,
		@RequestBody(required = false) @Valid OwnerApplicationApproveReqDto reqDto
	) {
		OwnerApplicationResDto response = ownerApplicationFacadeService.approve(
			applicationId,
			user.getUser().getId(),
			reqDto
		);
		return ResponseUtils.ok(response);
	}

	@Operation(
		summary = "가맹점주 신청 거절",
		description = "관리자가 신청을 거절합니다."
	)
	@PostMapping("/{applicationId}/reject")
	public ResponseEntity<BaseResponse<OwnerApplicationResDto>> reject(
		@PathVariable Long applicationId,
		@CurrentUser AuthenticatedUser user,
		@RequestBody @Valid OwnerApplicationRejectReqDto reqDto
	) {
		OwnerApplicationResDto response = ownerApplicationFacadeService.reject(
			applicationId,
			user.getUser().getId(),
			reqDto
		);
		return ResponseUtils.ok(response);
	}
}
