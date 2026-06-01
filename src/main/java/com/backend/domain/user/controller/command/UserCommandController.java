package com.backend.domain.user.controller.command;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.common.annotation.CurrentUser;
import com.backend.common.auth.principal.AuthenticatedUser;
import com.backend.common.response.BaseResponse;
import com.backend.common.util.ResponseUtils;
import com.backend.domain.user.dto.request.UserRegisterReqDto;
import com.backend.domain.user.dto.request.UserUpdateReqDto;
import com.backend.domain.user.dto.response.UserInfoResDto;
import com.backend.domain.user.service.facade.UserFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "User", description = "유저 관련 API")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCommandController {

	private final UserFacadeService userFacadeService;

	@Operation(
		summary = "사용자 서비스 등록",
		description = "소셜 로그인 후 닉네임과 역할을 설정하여 서비스에 최종 등록합니다. GUEST 상태의 사용자만 등록 가능합니다."
	)
	@PostMapping("/register")
	public ResponseEntity<BaseResponse<UserInfoResDto>> userRegistration(
		@CurrentUser final AuthenticatedUser token,
		@RequestBody @Valid final UserRegisterReqDto reqDto
	) {
		UserInfoResDto response = userFacadeService.registerUser(token.getUser().getId(), reqDto);
		return ResponseUtils.ok(response);
	}

	@Operation(
		summary = "사용자 프로필 수정",
		description = "닉네임, 프로필 이미지를 수정합니다. 전달된 필드만 업데이트됩니다."
	)
	@PutMapping
	public ResponseEntity<BaseResponse<UserInfoResDto>> updateProfile(
		@CurrentUser final AuthenticatedUser token,
		@RequestBody @Valid final UserUpdateReqDto reqDto
	) {
		UserInfoResDto response = userFacadeService.updateProfile(token.getUser().getId(), reqDto);
		return ResponseUtils.ok(response);
	}

}
