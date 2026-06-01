package com.backend.domain.user.mapper.query;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.backend.common.mapper.QueryMapper;
import com.backend.domain.user.entity.User;

@Mapper
public interface UserQueryMapper extends QueryMapper<User> {
	@Override
	Optional<User> findById(@Param("userId") Long userId);

	Optional<User> findByEmail(@Param("email") String email);

	Optional<User> findBySocialId(@Param("socialId") String socialId);

	boolean existBySocialId(@Param("socialId") String socialId);

	boolean existByNickname(@Param("nickname") String nickname);
}
