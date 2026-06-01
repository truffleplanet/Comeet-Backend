package com.backend.domain.user.service.command;

import com.backend.domain.user.dto.request.UserRegisterReqDto;
import com.backend.domain.user.dto.request.UserUpdateReqDto;
import com.backend.domain.user.entity.Role;
import com.backend.domain.user.entity.User;

public interface UserCommandService {
	User save(User user);

	int updateUserInfo(Long userId, UserRegisterReqDto reqDto);

	int updateProfile(Long userId, UserUpdateReqDto reqDto);

	int updateRole(Long userId, Role role);
}
