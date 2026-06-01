package com.backend.domain.user.service.facade;

import org.springframework.stereotype.Service;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.user.dto.request.UserRegisterReqDto;
import com.backend.domain.user.dto.request.UserRoleUpdateReqDto;
import com.backend.domain.user.dto.request.UserUpdateReqDto;
import com.backend.domain.user.dto.response.NicknameDuplicateResDto;
import com.backend.domain.user.dto.response.UserInfoResDto;
import com.backend.domain.user.entity.Role;
import com.backend.domain.user.service.command.UserCommandService;
import com.backend.domain.user.service.query.UserQueryService;
import com.backend.domain.user.validator.UserValidator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFacadeService {

	private final UserCommandService commandService;
	private final UserQueryService queryService;
	private final UserValidator userValidator;

	public UserInfoResDto registerUser(Long userId, UserRegisterReqDto reqDto) {
		userValidator.validate(reqDto);
		commandService.updateUserInfo(userId, reqDto);
		return queryService.findById(userId);
	}

	public UserInfoResDto findUserInfo(Long userId) {
		return queryService.findById(userId);
	}

	public NicknameDuplicateResDto checkNicknameDuplicate(String nickname) {
		return queryService.checkNicknameDuplicate(nickname);
	}

	public UserInfoResDto updateProfile(Long userId, UserUpdateReqDto reqDto) {
		UserInfoResDto currentUser = queryService.findById(userId);

		if (reqDto.nickname() != null) {
			userValidator.validateNickname(reqDto.nickname());

			if (!reqDto.nickname().equals(currentUser.nickname())) {
				NicknameDuplicateResDto duplicateCheck = queryService.checkNicknameDuplicate(reqDto.nickname());
				if (duplicateCheck.exists()) {
					throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
				}
			}
		}

		commandService.updateProfile(userId, reqDto);
		return queryService.findById(userId);
	}

	public UserInfoResDto updateRole(Long userId, UserRoleUpdateReqDto reqDto) {
		UserInfoResDto currentUser = queryService.findById(userId);

		if (Role.isNotActiveUser(currentUser.role())) {
			throw new BusinessException(ErrorCode.ROLE_CHANGE_NOT_ALLOWED);
		}

		if (reqDto.role() != Role.USER && reqDto.role() != Role.MANAGER) {
			throw new BusinessException(ErrorCode.INVALID_ROLE);
		}

		if (currentUser.role() != Role.MANAGER && reqDto.role() == Role.MANAGER) {
			throw new BusinessException(ErrorCode.ROLE_CHANGE_NOT_ALLOWED);
		}

		commandService.updateRole(userId, reqDto);
		return queryService.findById(userId);
	}
}
