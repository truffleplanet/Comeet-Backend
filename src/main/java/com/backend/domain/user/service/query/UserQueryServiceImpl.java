package com.backend.domain.user.service.query;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.user.converter.UserConverter;
import com.backend.domain.user.dto.response.NicknameDuplicateResDto;
import com.backend.domain.user.dto.response.UserInfoResDto;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.mapper.query.UserQueryMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class UserQueryServiceImpl implements UserQueryService {

	private final UserQueryMapper queryMapper;

	@Override
	public UserInfoResDto findById(final Long userId) {
		User user = queryMapper.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		log.info("[User] 사용자 조회 완료 - Id: {}", user.getId());
		return UserConverter.toResponse(user);
	}

	@Override
	public Optional<User> findBySocialId(final String socialId) {
		log.info("[User] 사용자 조회 - socialId: {}", socialId);
		return queryMapper.findBySocialId(socialId);
	}

	@Override
	public NicknameDuplicateResDto checkNicknameDuplicate(final String nickname) {
		log.info("[User] 닉네임 중복 조회 - nickname: {}", nickname);
		Boolean exists = queryMapper.existByNickname(nickname);
		return UserConverter.toNicknameDuplicateResDto(nickname, exists);
	}

	@Override
	public User findUnRegisterUserById(final Long userId) {
		log.info("[User] 사용자 조회 - userId: {}", userId);
		return queryMapper.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}
}
