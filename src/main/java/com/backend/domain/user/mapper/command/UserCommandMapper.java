package com.backend.domain.user.mapper.command;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.backend.common.mapper.CommandMapper;
import com.backend.domain.user.dto.request.UserRegisterReqDto;
import com.backend.domain.user.dto.request.UserUpdateReqDto;
import com.backend.domain.user.entity.Role;
import com.backend.domain.user.entity.User;

@Mapper
public interface UserCommandMapper extends CommandMapper<User> {
	int save(@Param("user") User user);

	int register(@Param("userId") Long userId, @Param("reqDto") UserRegisterReqDto reqDto);

	int updateProfile(@Param("userId") Long userId, @Param("reqDto") UserUpdateReqDto reqDto);

	int updateRole(@Param("userId") Long userId, @Param("role") Role role);
}
