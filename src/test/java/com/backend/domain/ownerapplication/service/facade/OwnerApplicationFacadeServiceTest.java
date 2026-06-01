package com.backend.domain.ownerapplication.service.facade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.ownerapplication.dto.request.OwnerApplicationCreateReqDto;
import com.backend.domain.ownerapplication.dto.request.OwnerApplicationRejectReqDto;
import com.backend.domain.ownerapplication.dto.response.OwnerApplicationResDto;
import com.backend.domain.ownerapplication.entity.OwnerApplication;
import com.backend.domain.ownerapplication.entity.OwnerApplicationStatus;
import com.backend.domain.ownerapplication.service.command.OwnerApplicationCommandService;
import com.backend.domain.ownerapplication.service.query.OwnerApplicationQueryService;
import com.backend.domain.store.dto.request.StoreCreateReqDto;
import com.backend.domain.store.service.facade.StoreFacadeService;
import com.backend.domain.user.dto.request.UserRoleUpdateReqDto;
import com.backend.domain.user.dto.response.UserInfoResDto;
import com.backend.domain.user.entity.Role;
import com.backend.domain.user.service.command.UserCommandService;
import com.backend.domain.user.service.query.UserQueryService;

@ExtendWith(MockitoExtension.class)
@DisplayName("OwnerApplicationFacadeService 테스트")
class OwnerApplicationFacadeServiceTest {

	@Mock
	private OwnerApplicationCommandService commandService;

	@Mock
	private OwnerApplicationQueryService queryService;

	@Mock
	private StoreFacadeService storeFacadeService;

	@Mock
	private UserCommandService userCommandService;

	@Mock
	private UserQueryService userQueryService;

	@InjectMocks
	private OwnerApplicationFacadeService ownerApplicationFacadeService;

	@Test
	@DisplayName("일반 사용자는 가맹점주 신청을 생성할 수 있다")
	void apply_Success_User() {
		// given
		Long userId = 1L;
		OwnerApplicationCreateReqDto reqDto = createReqDto();
		OwnerApplication application = OwnerApplication.create(userId, reqDto);
		when(userQueryService.findById(userId)).thenReturn(userInfo(userId, Role.USER));
		when(queryService.existsPendingByUserId(userId)).thenReturn(false);
		when(commandService.save(any(OwnerApplication.class))).thenReturn(application);

		// when
		OwnerApplicationResDto result = ownerApplicationFacadeService.apply(userId, reqDto);

		// then
		assertThat(result.status()).isEqualTo(OwnerApplicationStatus.PENDING);
		assertThat(result.userId()).isEqualTo(userId);
		verify(commandService, times(1)).save(any(OwnerApplication.class));
	}

	@Test
	@DisplayName("MANAGER는 가맹점주 신청을 생성할 수 없다")
	void apply_Fail_ManagerCannotApply() {
		// given
		Long userId = 1L;
		when(userQueryService.findById(userId)).thenReturn(userInfo(userId, Role.MANAGER));

		// when & then
		assertThatThrownBy(() -> ownerApplicationFacadeService.apply(userId, createReqDto()))
			.isInstanceOf(BusinessException.class)
			.satisfies(exception -> {
				BusinessException businessException = (BusinessException)exception;
				assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.OWNER_APPLICATION_NOT_ALLOWED);
			});

		verify(commandService, never()).save(any());
	}

	@Test
	@DisplayName("처리 대기 중인 신청이 있으면 중복 신청할 수 없다")
	void apply_Fail_AlreadyPending() {
		// given
		Long userId = 1L;
		when(userQueryService.findById(userId)).thenReturn(userInfo(userId, Role.USER));
		when(queryService.existsPendingByUserId(userId)).thenReturn(true);

		// when & then
		assertThatThrownBy(() -> ownerApplicationFacadeService.apply(userId, createReqDto()))
			.isInstanceOf(BusinessException.class)
			.satisfies(exception -> {
				BusinessException businessException = (BusinessException)exception;
				assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.OWNER_APPLICATION_ALREADY_PENDING);
			});

		verify(commandService, never()).save(any());
	}

	@Test
	@DisplayName("ADMIN 승인 시 가맹점을 생성하고 사용자를 MANAGER로 승격한다")
	void approve_Success() {
		// given
		Long applicationId = 10L;
		Long adminId = 99L;
		Long userId = 1L;
		OwnerApplication pending = application(applicationId, userId, OwnerApplicationStatus.PENDING);
		OwnerApplication approved = pending.toBuilder()
			.status(OwnerApplicationStatus.APPROVED)
			.reviewedBy(adminId)
			.build();
		when(queryService.findById(applicationId)).thenReturn(pending, approved);
		when(userCommandService.updateRole(eq(userId), any(UserRoleUpdateReqDto.class))).thenReturn(1);

		// when
		OwnerApplicationResDto result = ownerApplicationFacadeService.approve(applicationId, adminId);

		// then
		assertThat(result.status()).isEqualTo(OwnerApplicationStatus.APPROVED);
		verify(storeFacadeService, times(1)).createStore(any(StoreCreateReqDto.class), eq(userId));
		verify(userCommandService, times(1)).updateRole(
			eq(userId),
			argThat(reqDto -> reqDto.role() == Role.MANAGER)
		);
		verify(commandService, times(1)).approve(applicationId, adminId);
	}

	@Test
	@DisplayName("처리 대기 상태가 아닌 신청은 거절할 수 없다")
	void reject_Fail_NotPending() {
		// given
		Long applicationId = 10L;
		Long adminId = 99L;
		when(queryService.findById(applicationId))
			.thenReturn(application(applicationId, 1L, OwnerApplicationStatus.APPROVED));

		// when & then
		assertThatThrownBy(() -> ownerApplicationFacadeService.reject(
			applicationId,
			adminId,
			new OwnerApplicationRejectReqDto("reason")
		))
			.isInstanceOf(BusinessException.class)
			.satisfies(exception -> {
				BusinessException businessException = (BusinessException)exception;
				assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.OWNER_APPLICATION_NOT_PENDING);
			});

		verify(commandService, never()).reject(anyLong(), anyLong(), anyString());
	}

	private OwnerApplicationCreateReqDto createReqDto() {
		return new OwnerApplicationCreateReqDto(
			1L,
			"Comeet Cafe",
			"Specialty coffee",
			"Seoul",
			new BigDecimal("37.5012"),
			new BigDecimal("127.0396"),
			"09:00-22:00",
			"SPECIALTY_COFFEE",
			"02-1234-5678",
			"https://example.com/thumbnail.jpg"
		);
	}

	private OwnerApplication application(Long applicationId, Long userId, OwnerApplicationStatus status) {
		OwnerApplicationCreateReqDto reqDto = createReqDto();
		return OwnerApplication.create(userId, reqDto)
			.toBuilder()
			.id(applicationId)
			.status(status)
			.build();
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
