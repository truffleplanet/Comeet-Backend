package com.backend.common.auth.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.backend.common.auth.constants.AuthConstant;
import com.backend.common.auth.dto.Token;
import com.backend.common.auth.principal.DefaultAuthenticatedUser;
import com.backend.common.auth.principal.AuthenticatedUser;
import com.backend.common.auth.redis.BlackList;
import com.backend.common.auth.redis.repository.BlackListRepository;
import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.mapper.query.UserQueryMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 토큰 생성 및 검증
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
	private static final String ACCESS_TOKEN = "ACCESS_TOKEN";
	private static final String REFRESH_TOKEN = "REFRESH_TOKEN";
	private static final String TYPE = "type";
	public static final String BLANK = "";

	private final JwtProperties jwtProperties;
	private final BlackListRepository blackListRepository;
	private final UserQueryMapper queryMapper;

	public Token createToken(final User user) {
		return new Token(
			generateAccessToken(user),
			generateRefreshToken(user)
		);
	}

	private SecretKey generateSecretKey() {
		return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
	}

	private String generateAccessToken(final User user) {
		return generateToken(user, ACCESS_TOKEN, jwtProperties.accessTokenExpiration());
	}

	private String generateRefreshToken(final User user) {
		return generateToken(user, REFRESH_TOKEN, jwtProperties.refreshTokenExpiration());
	}

	private String generateToken(final User user, final String tokenType, final Long expiration) {
		Claims claims = Jwts.claims()
			.subject(user.getSocialId())
			.add(TYPE, tokenType)
			.issuedAt(new Date())
			.expiration(new Date(System.currentTimeMillis() + expiration))
			.build();

		return Jwts.builder()
			.claims(claims)
			.signWith(generateSecretKey())
			.compact();
	}

	public void validateToken(final String accessToken) {
		validateUndeformedToken(accessToken);

		Claims claims = getClaims(accessToken);
		validateTokenType(claims, ACCESS_TOKEN);

		if (blackListRepository.existsById(accessToken)) {
			throw new BusinessException(ErrorCode.TOKEN_BLACKLISTED_EXCEPTION);
		}
	}

	public Optional<String> extractAccessToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(AuthConstant.AUTHORIZATION)).filter(
			accessToken -> accessToken.startsWith(AuthConstant.BEARER)
		).map(accessToken -> accessToken.replace(AuthConstant.BEARER, BLANK));
	}

	public Optional<String> getType(Claims claims) {
		return Optional.of(claims.get(TYPE, String.class));
	}

	private Claims getClaims(final String token) {
		try {
			return Jwts.parser()
				.verifyWith(Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8)))
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (ExpiredJwtException e) {
			throw new BusinessException(ErrorCode.TOKEN_EXPIRED_EXCEPTION);
		} catch (MalformedJwtException | IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN);
		} catch (SignatureException e) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN_SIGNATURE);
		} catch (UnsupportedJwtException e) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN_TYPE);
		} catch (JwtException e) {
			throw new BusinessException(ErrorCode.TOKEN_PROCESSING_ERROR);
		}
	}

	public Optional<User> getUser(String token) {
		Claims claims = getClaims(token);
		final String socialId = claims.getSubject();
		return queryMapper.findBySocialId(socialId);
	}

	public void setBlackList(final String refreshToken) {
		BlackList blackList = BlackList.builder()
			.id(refreshToken)
			.build();
		blackListRepository.save(blackList);
	}

	public AuthenticatedUser getAuthenticatedUser(final String accessToken) {
		Claims claims = getClaims(accessToken);
		validateTokenType(claims, ACCESS_TOKEN);

		return queryMapper.findBySocialId(claims.getSubject())
			.map(DefaultAuthenticatedUser::new)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}

	private void validateTokenType(Claims claims, String tokenType) {
		getType(claims)
			.filter(type -> type.equals(tokenType))
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN_TYPE));
	}

	private void validateUndeformedToken(String accessToken) {
		if (accessToken == null || accessToken.isEmpty()) {
			throw new BusinessException(ErrorCode.MALFORMED_TOKEN_EXCEPTION);
		}
	}
}
