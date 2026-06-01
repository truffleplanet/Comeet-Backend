package com.backend.domain.beanscore.service.query;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.beanscore.dto.response.BeanScoreWithBeanDto;
import com.backend.domain.beanscore.entity.BeanScore;
import com.backend.domain.beanscore.mapper.query.BeanScoreQueryMapper;

import lombok.RequiredArgsConstructor;

/**
 * BeanScore Query Service 구현체
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BeanScoreQueryServiceImpl implements BeanScoreQueryService {

	private final BeanScoreQueryMapper beanScoreQueryMapper;

	@Override
	public Optional<BeanScore> findByBeanId(Long beanId) {
		return beanScoreQueryMapper.findByBeanId(beanId);
	}

	@Override
	public BeanScore getByBeanId(Long beanId) {
		return beanScoreQueryMapper.findByBeanId(beanId)
			.orElseThrow(() -> new BusinessException(ErrorCode.BEAN_SCORE_NOT_FOUND));
	}

	@Override
	public boolean existsByBeanId(Long beanId) {
		return beanScoreQueryMapper.existsByBeanId(beanId);
	}

	@Override
	public List<BeanScore> findAll() {
		return beanScoreQueryMapper.findAll();
	}

	@Override
	public Optional<BeanScoreWithBeanDto> findWithBeanByBeanId(Long beanId) {
		return beanScoreQueryMapper.findWithBeanByBeanId(beanId);
	}

	@Override
	public List<BeanScoreWithBeanDto> findWithBeanByBeanIds(List<Long> beanIds) {
		if (beanIds == null || beanIds.isEmpty()) {
			return List.of();
		}
		return beanScoreQueryMapper.findWithBeanByBeanIds(beanIds);
	}

	@Override
	public List<BeanScoreWithBeanDto> findFilteredBeanScores(
		List<String> dislikedTags,
		List<String> preferredRoastLevels
	) {
		return beanScoreQueryMapper.findFilteredBeanScores(dislikedTags, preferredRoastLevels);
	}
}
