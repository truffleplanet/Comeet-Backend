package com.backend.domain.visit.service.query;

import static lombok.AccessLevel.PROTECTED;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.visit.entity.Visit;
import com.backend.domain.visit.mapper.query.VisitQueryMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = PROTECTED)
public class VisitQueryServiceImpl implements VisitQueryService {
	private final VisitQueryMapper queryMapper;

	@Override
	public Visit findById(final Long visitId) {
		Visit visit = queryMapper.findById(visitId)
			.orElseThrow(() -> new BusinessException(ErrorCode.VISIT_NOT_FOUND));
		log.info("[Visit] 방문 기록 상세 조회 id : {}", visit.getId());
		return visit;
	}

	@Override
	public List<Visit> findAllByUserId(final Long userId, final Pageable pageable) {
		List<Visit> list = queryMapper.findAllByUserId(userId, pageable);
		log.info("[Visit] 사용자의 모든 방문 기록 조회 size: {}", list.size());
		return list;
	}

	@Override
	public int countAllByUserId(final Long userId) {
		return queryMapper.countAllByUserId(userId);
	}
}
