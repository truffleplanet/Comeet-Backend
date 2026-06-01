package com.backend.domain.auth.controller.command;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.common.auth.dto.Token;
import com.backend.common.response.BaseResponse;
import com.backend.common.util.ResponseUtils;
import com.backend.domain.auth.dto.request.AuthLoginReqDto;
import com.backend.domain.auth.dto.request.AuthSignupReqDto;
import com.backend.domain.auth.service.command.AuthCommandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthCommandController {

	private final AuthCommandService authCommandService;

	@Operation(summary = "회원가입", description = "이메일과 비밀번호로 회원가입하고 JWT를 발급합니다.")
	@PostMapping("/signup")
	public ResponseEntity<BaseResponse<Token>> signup(
		@RequestBody @Valid AuthSignupReqDto reqDto,
		HttpServletResponse response
	) {
		return ResponseUtils.created(authCommandService.signup(reqDto, response));
	}

	@Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 JWT를 발급합니다.")
	@PostMapping("/login")
	public ResponseEntity<BaseResponse<Token>> login(
		@RequestBody @Valid AuthLoginReqDto reqDto,
		HttpServletResponse response
	) {
		return ResponseUtils.ok(authCommandService.login(reqDto, response));
	}

	@Operation(summary = "로그아웃", description = "액세스 토큰과 리프레시 토큰을 블랙리스트에 추가하고 Redis에서 삭제합니다.")
	@PostMapping("/logout")
	public ResponseEntity<BaseResponse<Void>> logout(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		authCommandService.logout(request, response);
		return ResponseUtils.noContent();
	}

	@Operation(summary = "토큰 재발급", description = "RefreshToken을 사용하여 새로운 AccessToken과 RefreshToken을 발급합니다.")
	@PostMapping("/reissue")
	public ResponseEntity<BaseResponse<Void>> reissueToken(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		authCommandService.reissue(request, response);
		return ResponseUtils.noContent();
	}
}
