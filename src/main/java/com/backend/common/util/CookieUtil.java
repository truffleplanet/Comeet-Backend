package com.backend.common.util;

import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CookieUtil {

	private static final String REFRESH_TOKEN_NAME = "refreshToken";
	private static final String PATH = "/";

	public Cookie generateCookie(final String refreshToken, final Long refreshTokenExpiration) {
		Cookie cookie = new Cookie(REFRESH_TOKEN_NAME, refreshToken);
		cookie.setPath(PATH);
		cookie.setMaxAge((int)(refreshTokenExpiration / 1000));
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		return cookie;
	}

	public Cookie deleteRefreshCookie(final String refreshToken) {
		Cookie cookie = new Cookie(REFRESH_TOKEN_NAME, refreshToken);
		cookie.setPath(PATH);
		cookie.setMaxAge(0);
		cookie.setSecure(true);
		cookie.setHttpOnly(true);
		return cookie;
	}

	public Optional<String> extractRefreshToken(final HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (REFRESH_TOKEN_NAME.equals(cookie.getName())) {
					return Optional.of(cookie.getValue());
				}
			}
		}
		return Optional.empty();
	}
}
