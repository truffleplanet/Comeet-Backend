package com.backend.domain.auth.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.common.auth.dto.Token;
import com.backend.common.response.BaseResponse;
import com.backend.common.util.ResponseUtils;
import com.backend.domain.auth.service.command.AuthCommandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "Dev Auth", description = "로컬 개발용 인증 API")
@Profile("local")
@RestController
@RequestMapping("/dev/auth")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DevAuthController {

	private final AuthCommandService authCommandService;

	@Operation(
		summary = "로컬 개발용 토큰 발급",
		description = "local 프로필에서만 사용할 수 있습니다. DB에 존재하는 userId로 JWT를 발급합니다."
	)
	@PostMapping("/token")
	public ResponseEntity<BaseResponse<Token>> issueToken(
		@RequestParam Long userId,
		HttpServletResponse response
	) {
		return ResponseUtils.ok(authCommandService.issueTokenForUser(userId, response));
	}
}
