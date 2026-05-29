package com.backend.domain.review.service.query.implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.review.entity.CuppingNote;
import com.backend.domain.review.mapper.query.CuppingNoteQueryMapper;
import com.backend.domain.review.service.query.CuppingNoteQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CuppingNoteQueryServiceImpl implements CuppingNoteQueryService {
	private final CuppingNoteQueryMapper queryMapper;

	@Override
	public CuppingNote findByReviewId(final Long reviewId) {
		return queryMapper.findByReviewId(reviewId)
			.map(cuppingNote -> {
				log.debug("[CuppingNote] 커핑 노트 조회 완료 - id: {}, reviewId: {}", cuppingNote.getId(), reviewId);
				return cuppingNote;
			})
			.orElseThrow(() -> {
				log.warn("[CuppingNote] 커핑 노트 조회 실패 - reviewId: {}", reviewId);
				return new BusinessException(ErrorCode.CUPPING_NOTE_NOT_FOUND);
			});
	}

	@Override
	public boolean existsByReviewId(final Long reviewId) {
		return queryMapper.existsByReviewId(reviewId);
	}
}
