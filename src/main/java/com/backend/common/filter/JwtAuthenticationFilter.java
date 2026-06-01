package com.backend.common.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.backend.common.auth.constants.AuthConstant;
import com.backend.common.auth.jwt.JwtTokenProvider;
import com.backend.common.auth.principal.AuthenticatedUser;
import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	// 모든 HTTP 메서드에서 JWT 검증 스킵
	private static final String[] WHITELIST = {
		// Web
		"/",
		"/error",
		"/favicon.ico",

		// Swagger
		"/v3/api-docs/**",
		"/swagger-ui/**",
		"/swagger-resources/**",

		// Actuator
		"/actuator",
		"/actuator/**",

		// Auth
		"/auth/signup",
		"/auth/login",
		"/auth/reissue",

		// Local development
		"/dev/**"
	};

	// GET 요청에서만 JWT 검증 스킵하는 경로 (목록 조회)
	private static final String[] GET_ONLY_WHITELIST = {
		"/stores",
		"/beans",
		"/flavors"
	};

	// 숫자 ID를 포함하는 공개 API 패턴 (예: /stores/123, /beans/456)
	private static final String[] NUMERIC_ID_PATTERNS = {
		"/stores/{id}",
		"/stores/{id}/menus",
		"/stores/{id}/reviews",
		"/menus/{id}",
		"/beans/{id}"
	};

	@Override
	protected void doFilterInternal(
		HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain
	) throws ServletException, IOException {

		String requestURI = request.getRequestURI();
		String method = request.getMethod();

		// WHITELIST 경로는 JWT 검증 스킵
		if (isWhitelisted(requestURI, method)) {
			filterChain.doFilter(request, response);
			return;
		}

		final String authorizationHeader = request.getHeader(AuthConstant.AUTHORIZATION);

		// Authorization 헤더가 없거나 Bearer로 시작하지 않으면 필터 통과
		// (SecurityConfig의 authenticated()가 처리)
		if (authorizationHeader == null || !authorizationHeader.startsWith(AuthConstant.BEARER)) {
			filterChain.doFilter(request, response);
			return;
		}

		final String bearerToken = getBearerToken(authorizationHeader);
		jwtTokenProvider.validateToken(bearerToken);
		setAuthentication(bearerToken);

		filterChain.doFilter(request, response);
	}

	private boolean isWhitelisted(String requestURI, String method) {
		// 기본 WHITELIST 체크 (모든 HTTP 메서드 허용)
		if (Arrays.stream(WHITELIST)
			.anyMatch(pattern -> pathMatcher.match(pattern, requestURI))) {
			return true;
		}

		// GET 요청이 아니면 이후 체크 불필요
		if (!"GET".equalsIgnoreCase(method)) {
			return false;
		}

		// GET 전용 WHITELIST 체크 (목록 조회 API)
		if (Arrays.stream(GET_ONLY_WHITELIST)
			.anyMatch(pattern -> pathMatcher.match(pattern, requestURI))) {
			return true;
		}

		// 숫자 ID 패턴 체크 (GET 요청만)
		return isNumericIdPath(requestURI);
	}

	/**
	 * 숫자 ID를 포함하는 공개 API 경로인지 확인
	 * 예: /stores/123 → true, /stores/my → false
	 */
	private boolean isNumericIdPath(String requestURI) {
		for (String pattern : NUMERIC_ID_PATTERNS) {
			if (matchesNumericPattern(requestURI, pattern)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * URI가 숫자 ID 패턴과 매칭되는지 확인
	 * {id} 부분이 실제로 숫자인 경우에만 true 반환
	 */
	private boolean matchesNumericPattern(String requestURI, String pattern) {
		String[] uriParts = requestURI.split("/");
		String[] patternParts = pattern.split("/");

		if (uriParts.length != patternParts.length) {
			return false;
		}

		for (int i = 0; i < patternParts.length; i++) {
			String patternPart = patternParts[i];
			String uriPart = uriParts[i];

			if ("{id}".equals(patternPart)) {
				// {id} 위치의 값이 숫자인지 확인
				if (!isNumeric(uriPart)) {
					return false;
				}
			} else if (!patternPart.equals(uriPart)) {
				return false;
			}
		}
		return true;
	}

	private boolean isNumeric(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}
		for (char c : str.toCharArray()) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}

	private void setAuthentication(String accessToken) {
		AuthenticatedUser authenticatedUser = jwtTokenProvider.getAuthenticatedUser(accessToken);
		Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
			authenticatedUser,
			"",
			List.of(new SimpleGrantedAuthority(authenticatedUser.getRole().getKey()))
		);
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);
	}

	private String getBearerToken(String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith(AuthConstant.BEARER)) {
			throw new BusinessException(ErrorCode.MALFORMED_TOKEN_EXCEPTION);
		}
		return authorizationHeader.replace(AuthConstant.BEARER, "");
	}

}
