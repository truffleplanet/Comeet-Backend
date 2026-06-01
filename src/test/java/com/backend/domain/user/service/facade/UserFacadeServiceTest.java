package com.backend.domain.user.service.facade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.user.dto.request.UserRoleUpdateReqDto;
import com.backend.domain.user.dto.response.UserInfoResDto;
import com.backend.domain.user.entity.Role;
import com.backend.domain.user.service.command.UserCommandService;
import com.backend.domain.user.service.query.UserQueryService;
import com.backend.domain.user.validator.UserValidator;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserFacadeService 테스트")
class UserFacadeServiceTest {

	@Mock
	private UserCommandService commandService;

	@Mock
	private UserQueryService queryService;

	@Mock
	private UserValidator userValidator;

	@InjectMocks
	private UserFacadeService userFacadeService;

	@Test
	@DisplayName("일반 사용자는 MANAGER로 자기 승격할 수 없다")
	void updateRole_Fail_UserCannotPromoteToManager() {
		// given
		Long userId = 1L;
		UserRoleUpdateReqDto reqDto = new UserRoleUpdateReqDto(Role.MANAGER);
		when(queryService.findById(userId)).thenReturn(userInfo(userId, Role.USER));

		// when & then
		assertThatThrownBy(() -> userFacadeService.updateRole(userId, reqDto))
			.isInstanceOf(BusinessException.class)
			.satisfies(exception -> {
				BusinessException businessException = (BusinessException)exception;
				assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.ROLE_CHANGE_NOT_ALLOWED);
			});

		verify(commandService, never()).updateRole(anyLong(), any());
	}

	@Test
	@DisplayName("GUEST 상태 사용자는 역할을 변경할 수 없다")
	void updateRole_Fail_GuestCannotChangeRole() {
		// given
		Long userId = 1L;
		UserRoleUpdateReqDto reqDto = new UserRoleUpdateReqDto(Role.USER);
		when(queryService.findById(userId)).thenReturn(userInfo(userId, Role.GUEST));

		// when & then
		assertThatThrownBy(() -> userFacadeService.updateRole(userId, reqDto))
			.isInstanceOf(BusinessException.class)
			.satisfies(exception -> {
				BusinessException businessException = (BusinessException)exception;
				assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.ROLE_CHANGE_NOT_ALLOWED);
			});

		verify(commandService, never()).updateRole(anyLong(), any());
	}

	@Test
	@DisplayName("일반 사용자는 USER 역할 유지 요청을 처리할 수 있다")
	void updateRole_Success_UserKeepsUserRole() {
		// given
		Long userId = 1L;
		UserRoleUpdateReqDto reqDto = new UserRoleUpdateReqDto(Role.USER);
		when(queryService.findById(userId))
			.thenReturn(userInfo(userId, Role.USER))
			.thenReturn(userInfo(userId, Role.USER));
		when(commandService.updateRole(userId, reqDto)).thenReturn(1);

		// when
		UserInfoResDto result = userFacadeService.updateRole(userId, reqDto);

		// then
		assertThat(result.role()).isEqualTo(Role.USER);
		verify(commandService, times(1)).updateRole(userId, reqDto);
	}

	@Test
	@DisplayName("역할 변경 API로 ADMIN 역할을 부여할 수 없다")
	void updateRole_Fail_AdminRole() {
		// given
		Long userId = 1L;
		UserRoleUpdateReqDto reqDto = new UserRoleUpdateReqDto(Role.ADMIN);
		when(queryService.findById(userId)).thenReturn(userInfo(userId, Role.MANAGER));

		// when & then
		assertThatThrownBy(() -> userFacadeService.updateRole(userId, reqDto))
			.isInstanceOf(BusinessException.class)
			.satisfies(exception -> {
				BusinessException businessException = (BusinessException)exception;
				assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.INVALID_ROLE);
			});

		verify(commandService, never()).updateRole(anyLong(), any());
	}

	private UserInfoResDto userInfo(Long userId, Role role) {
		return UserInfoResDto.builder()
			.userId(userId)
			.name("tester")
			.email("tester@example.com")
			.nickname("tester")
			.profileImageUrl(null)
			.role(role)
			.build();
	}
}
