package com.backend.domain.auth.service.command;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.common.auth.dto.Token;
import com.backend.common.auth.jwt.JwtProperties;
import com.backend.common.auth.jwt.JwtTokenProvider;
import com.backend.common.auth.redis.repository.BlackListRepository;
import com.backend.common.auth.redis.repository.RefreshTokenRepository;
import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.auth.dto.request.AuthLoginReqDto;
import com.backend.domain.user.entity.Role;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.mapper.query.UserQueryMapper;
import com.backend.domain.user.service.command.UserCommandService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthCommandService 테스트")
class AuthCommandServiceImplTest {

	@Mock
	private JwtProperties jwtProperties;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private BlackListRepository blackListRepository;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private UserCommandService userCommandService;

	@Mock
	private UserQueryMapper userQueryMapper;

	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	private AuthCommandServiceImpl authCommandService;

	@BeforeEach
	void setUp() {
		authCommandService = new AuthCommandServiceImpl(
			jwtProperties,
			jwtTokenProvider,
			blackListRepository,
			refreshTokenRepository,
			userCommandService,
			userQueryMapper,
			passwordEncoder
		);
	}

	@Test
	@DisplayName("이메일과 비밀번호가 일치하면 토큰을 발급한다")
	void login_Success() {
		// given
		User user = user("user@example.com", passwordEncoder.encode("password1234"), Role.USER);
		when(userQueryMapper.findByEmail("user@example.com")).thenReturn(Optional.of(user));
		when(jwtTokenProvider.createToken(user)).thenReturn(new Token("access-token", "refresh-token"));
		when(jwtProperties.refreshTokenExpiration()).thenReturn(1209600000L);

		// when
		Token token = authCommandService.login(
			new AuthLoginReqDto("user@example.com", "password1234"),
			new MockHttpServletResponse()
		);

		// then
		assertThat(token.accessToken()).isEqualTo("access-token");
		assertThat(token.refreshToken()).isEqualTo("refresh-token");
		verify(refreshTokenRepository).save(any());
	}

	@Test
	@DisplayName("비밀번호가 일치하지 않으면 인증 실패 예외를 던진다")
	void login_Fail_InvalidPassword() {
		// given
		User user = user("user@example.com", passwordEncoder.encode("password1234"), Role.USER);
		when(userQueryMapper.findByEmail("user@example.com")).thenReturn(Optional.of(user));

		// when & then
		assertThatThrownBy(() -> authCommandService.login(
			new AuthLoginReqDto("user@example.com", "wrong-password"),
			new MockHttpServletResponse()
		))
			.isInstanceOf(BusinessException.class)
			.satisfies(exception -> {
				BusinessException businessException = (BusinessException)exception;
				assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS);
			});

		verify(jwtTokenProvider, never()).createToken(any());
	}

	@Test
	@DisplayName("로컬 개발용 토큰은 userId로 사용자를 찾아 발급한다")
	void issueTokenForUser_Success() {
		// given
		User admin = user("admin@example.com", passwordEncoder.encode("password1234"), Role.ADMIN);
		when(userQueryMapper.findById(3L)).thenReturn(Optional.of(admin));
		when(jwtTokenProvider.createToken(admin)).thenReturn(new Token("admin-access-token", "admin-refresh-token"));
		when(jwtProperties.refreshTokenExpiration()).thenReturn(1209600000L);

		// when
		Token token = authCommandService.issueTokenForUser(3L, new MockHttpServletResponse());

		// then
		assertThat(token.accessToken()).isEqualTo("admin-access-token");
		verify(refreshTokenRepository).save(any());
	}

	private User user(String email, String encodedPassword, Role role) {
		return User.builder()
			.id(1L)
			.name("테스트")
			.email(email)
			.password(encodedPassword)
			.nickname("tester")
			.socialId(email)
			.role(role)
			.build();
	}
}
