package com.backend.domain.ownerapplication.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.common.annotation.CurrentUser;
import com.backend.common.auth.principal.AuthenticatedUser;
import com.backend.common.response.BaseResponse;
import com.backend.common.util.ResponseUtils;
import com.backend.domain.ownerapplication.dto.request.OwnerApplicationCreateReqDto;
import com.backend.domain.ownerapplication.dto.response.OwnerApplicationResDto;
import com.backend.domain.ownerapplication.service.facade.OwnerApplicationFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "Owner Application", description = "가맹점주 신청 API")
@RestController
@RequestMapping("/owner-applications")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class OwnerApplicationCommandController {

	private final OwnerApplicationFacadeService ownerApplicationFacadeService;

	@Operation(
		summary = "가맹점주 신청",
		description = "일반 사용자가 가맹점주 승격과 가맹점 등록을 신청합니다. 승인 전까지는 USER 권한이 유지됩니다."
	)
	@PostMapping
	public ResponseEntity<BaseResponse<OwnerApplicationResDto>> apply(
		@CurrentUser AuthenticatedUser user,
		@RequestBody @Valid OwnerApplicationCreateReqDto reqDto
	) {
		OwnerApplicationResDto response = ownerApplicationFacadeService.apply(user.getUser().getId(), reqDto);
		return ResponseUtils.created(response);
	}
}
