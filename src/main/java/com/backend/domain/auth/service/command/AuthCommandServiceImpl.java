package com.backend.domain.auth.service.command;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.backend.common.auth.constants.AuthConstant;
import com.backend.common.auth.dto.Token;
import com.backend.common.auth.jwt.JwtProperties;
import com.backend.common.auth.jwt.JwtTokenProvider;
import com.backend.common.auth.redis.RefreshToken;
import com.backend.common.auth.redis.repository.BlackListRepository;
import com.backend.common.auth.redis.repository.RefreshTokenRepository;
import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.util.CookieUtil;
import com.backend.domain.auth.dto.request.AuthLoginReqDto;
import com.backend.domain.auth.dto.request.AuthSignupReqDto;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.mapper.query.UserQueryMapper;
import com.backend.domain.user.service.command.UserCommandService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthCommandServiceImpl implements AuthCommandService {

	private final JwtProperties jwtProperties;
	private final JwtTokenProvider jwtTokenProvider;
	private final BlackListRepository blackListRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserCommandService userCommandService;
	private final UserQueryMapper userQueryMapper;
	private final PasswordEncoder passwordEncoder;

	@Override
	public Token signup(final AuthSignupReqDto reqDto, final HttpServletResponse response) {
		if (userQueryMapper.findByEmail(reqDto.email()).isPresent()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		if (userQueryMapper.existByNickname(reqDto.nickname())) {
			throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
		}

		User user = User.local(
			reqDto.name(),
			reqDto.email(),
			passwordEncoder.encode(reqDto.password()),
			reqDto.nickname()
		);

		User savedUser = userCommandService.save(user);
		log.info("[Auth] 이메일 회원가입 완료 - userId: {}, email: {}", savedUser.getId(), savedUser.getEmail());
		return issueToken(savedUser, response);
	}

	@Override
	public Token login(final AuthLoginReqDto reqDto, final HttpServletResponse response) {
		User user = userQueryMapper.findByEmail(reqDto.email())
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

		if (user.getPassword() == null || !passwordEncoder.matches(reqDto.password(), user.getPassword())) {
			throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
		}

		log.info("[Auth] 이메일 로그인 완료 - userId: {}, email: {}", user.getId(), user.getEmail());
		return issueToken(user, response);
	}

	@Override
	public Token issueTokenForUser(final Long userId, final HttpServletResponse response) {
		User user = userQueryMapper.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		log.info("[Auth] 로컬 개발용 토큰 발급 - userId: {}, role: {}", user.getId(), user.getRole());
		return issueToken(user, response);
	}

	@Override
	public void logout(
		final HttpServletRequest request,
		final HttpServletResponse response
	) {
		String refreshToken = CookieUtil.extractRefreshToken(request);

		User user = jwtTokenProvider.getUser(refreshToken)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		String accessToken = jwtTokenProvider.extractAccessToken(request)
			.orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_NOT_FOUND));

		jwtTokenProvider.setBlackList(refreshToken);
		jwtTokenProvider.setBlackList(accessToken);
		refreshTokenRepository.deleteById(user.getSocialId());

		Cookie cookie = CookieUtil.deleteRefreshCookie(refreshToken);
		response.addCookie(cookie);
	}

	@Override
	public void reissue(final HttpServletRequest request, final HttpServletResponse response) {
		String refreshToken = CookieUtil.extractRefreshToken(request);

		if (blackListRepository.existsById(refreshToken)) {
			throw new BusinessException(ErrorCode.TOKEN_BLACKLISTED_EXCEPTION);
		}

		User user = jwtTokenProvider.getUser(refreshToken)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		RefreshToken savedRefreshToken = refreshTokenRepository.findById(user.getSocialId())
			.orElseThrow(() -> new BusinessException(ErrorCode.TOKEN_NOT_FOUND));

		if (!refreshToken.equals(savedRefreshToken.getToken())) {
			throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_MATCH);
		}

		jwtTokenProvider.setBlackList(refreshToken);

		Token token = jwtTokenProvider.createToken(user);
		savedRefreshToken.updateToken(token.refreshToken());

		refreshTokenRepository.save(savedRefreshToken);

		response.addHeader(AuthConstant.AUTHORIZATION, AuthConstant.BEARER + token.accessToken());
		Cookie cookie = CookieUtil.generateCookie(token.refreshToken(),
			jwtProperties.refreshTokenExpiration());
		response.addCookie(cookie);
	}

	private Token issueToken(final User user, final HttpServletResponse response) {
		Token token = jwtTokenProvider.createToken(user);
		RefreshToken refreshToken = RefreshToken.builder()
			.socialId(user.getSocialId())
			.token(token.refreshToken())
			.expirationTime(jwtProperties.refreshTokenExpiration())
			.build();

		refreshTokenRepository.save(refreshToken);
		response.addHeader(AuthConstant.AUTHORIZATION, AuthConstant.BEARER + token.accessToken());
		response.addCookie(CookieUtil.generateCookie(token.refreshToken(), jwtProperties.refreshTokenExpiration()));
		return token;
	}
}
