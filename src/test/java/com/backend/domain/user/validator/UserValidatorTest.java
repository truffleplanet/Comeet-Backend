package com.backend.domain.user.validator;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.user.dto.request.UserRegisterReqDto;
import com.backend.domain.user.entity.Role;

@DisplayName("UserValidator 테스트")
class UserValidatorTest {

	private final UserValidator userValidator = new UserValidator();

	@Test
	@DisplayName("회원 등록 시 USER 역할은 허용한다")
	void validateRegister_Success_UserRole() {
		// given
		UserRegisterReqDto reqDto = new UserRegisterReqDto("tester", Role.USER);

		// when & then
		assertThatCode(() -> userValidator.validate(reqDto)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("회원 등록 시 OWNER 역할은 허용하지 않는다")
	void validateRegister_Fail_OwnerRole() {
		// given
		UserRegisterReqDto reqDto = new UserRegisterReqDto("tester", Role.OWNER);

		// when & then
		assertThatThrownBy(() -> userValidator.validate(reqDto))
			.isInstanceOf(BusinessException.class)
			.satisfies(exception -> {
				BusinessException businessException = (BusinessException)exception;
				assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.INVALID_ROLE);
			});
	}

	@Test
	@DisplayName("회원 등록 시 GUEST 역할은 허용하지 않는다")
	void validateRegister_Fail_GuestRole() {
		// given
		UserRegisterReqDto reqDto = new UserRegisterReqDto("tester", Role.GUEST);

		// when & then
		assertThatThrownBy(() -> userValidator.validate(reqDto))
			.isInstanceOf(BusinessException.class)
			.satisfies(exception -> {
				BusinessException businessException = (BusinessException)exception;
				assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.INVALID_ROLE);
			});
	}

	@Test
	@DisplayName("회원 등록 시 ADMIN 역할은 허용하지 않는다")
	void validateRegister_Fail_AdminRole() {
		// given
		UserRegisterReqDto reqDto = new UserRegisterReqDto("tester", Role.ADMIN);

		// when & then
		assertThatThrownBy(() -> userValidator.validate(reqDto))
			.isInstanceOf(BusinessException.class)
			.satisfies(exception -> {
				BusinessException businessException = (BusinessException)exception;
				assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.INVALID_ROLE);
			});
	}
}
