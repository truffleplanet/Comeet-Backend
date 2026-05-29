package com.backend.domain.preference.service.query;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.preference.entity.UserPreference;
import com.backend.domain.preference.mapper.query.PreferenceQueryMapper;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PreferenceQueryServiceImpl implements PreferenceQueryService {

	private final PreferenceQueryMapper preferenceQueryMapper;

	@Override
	public Optional<UserPreference> findByUserId(Long userId) {
		return preferenceQueryMapper.findByUserId(userId);
	}

	@Override
	public UserPreference getByUserId(Long userId) {
		return preferenceQueryMapper.findByUserId(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.PREFERENCE_NOT_FOUND));
	}

	@Override
	public boolean existsByUserId(Long userId) {
		return preferenceQueryMapper.existsByUserId(userId);
	}
}
