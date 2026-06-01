package com.backend.domain.auth.service.command;

import com.backend.common.auth.dto.Token;
import com.backend.domain.auth.dto.request.AuthLoginReqDto;
import com.backend.domain.auth.dto.request.AuthSignupReqDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthCommandService {

	Token signup(AuthSignupReqDto reqDto, HttpServletResponse response);

	Token login(AuthLoginReqDto reqDto, HttpServletResponse response);

	Token issueTokenForUser(Long userId, HttpServletResponse response);

	void logout(HttpServletRequest request, HttpServletResponse response);

	void reissue(HttpServletRequest request, HttpServletResponse response);
}
