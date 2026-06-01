package com.backend.domain.user.validator;

import org.springframework.stereotype.Component;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.validator.Validator;
import com.backend.domain.user.dto.request.UserRegisterReqDto;
import com.backend.domain.user.entity.Role;
import com.backend.domain.user.entity.User;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserValidator implements Validator<User> {

	private static final String NICKNAME_REGEX = "^[a-zA-Z가-힣0-9]++(?: [a-zA-Z가-힣0-9]++)*+$";

	@Override
	public void validate(final User user) {
		validateUserId(user.getId());
		validateUserRole(user.getRole());
	}

	public void validate(final UserRegisterReqDto reqDto) {
		validateNickname(reqDto.nickname());
		validateRegisterRole(reqDto.role());
	}

	public void validateNickname(final String nickname) {
		if (nickname == null || nickname.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		if (!nickname.matches(NICKNAME_REGEX)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}

	private void validateUserId(final Long userId) {
		if (userId == null) {
			throw new BusinessException(ErrorCode.INVALID_USER);
		}
	}

	private void validateUserRole(final Role role) {
		if (role == null || Role.isNotActiveUser(role)) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	private void validateRegisterRole(final Role role) {
		if (role != Role.USER) {
			throw new BusinessException(ErrorCode.INVALID_ROLE);
		}
	}
}
