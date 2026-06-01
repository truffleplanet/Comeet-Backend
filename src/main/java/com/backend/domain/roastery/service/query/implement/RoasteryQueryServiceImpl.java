package com.backend.domain.roastery.service.query.implement;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.roastery.entity.Roastery;
import com.backend.domain.roastery.mapper.query.RoasteryQueryMapper;
import com.backend.domain.roastery.service.query.RoasteryQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class RoasteryQueryServiceImpl implements RoasteryQueryService {
	private final RoasteryQueryMapper queryMapper;

	@Override
	public Roastery findById(final Long roasteryId) {
		Roastery roastery = queryMapper.findById(roasteryId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ROASTERY_NOT_FOUND));
		log.info("[Roastery] 로스터리 조회 - ID: {}", roastery.getId());
		return roastery;
	}

	@Override
	public List<Roastery> findAll(final Pageable pageable) {
		List<Roastery> list = queryMapper.findAll(pageable);
		log.info("[Roastery] 모든 로스터리 조회 - size: {}", list.size());
		return list;
	}

	@Override
	public int countAll() {
		return queryMapper.countAll();
	}

	@Override
	public List<Roastery> findByNameContaining(final String keyword, final Pageable pageable) {
		List<Roastery> list = queryMapper.findByNameContaining(keyword, pageable);
		log.info("[Roastery] 이름으로 로스터리 검색 - keyword: {}, size: {}", keyword, list.size());
		return list;
	}

	@Override
	public int countByNameContaining(final String keyword) {
		return queryMapper.countByNameContaining(keyword);
	}
}
