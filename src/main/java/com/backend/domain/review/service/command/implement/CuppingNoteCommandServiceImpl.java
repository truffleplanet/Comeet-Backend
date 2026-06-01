package com.backend.domain.review.service.command.implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.review.entity.CuppingNote;
import com.backend.domain.review.mapper.command.CuppingNoteCommandMapper;
import com.backend.domain.review.service.command.CuppingNoteCommandService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CuppingNoteCommandServiceImpl implements CuppingNoteCommandService {
	private final CuppingNoteCommandMapper commandMapper;

	@Override
	public int insert(final CuppingNote cuppingNote) {
		final int result = commandMapper.insert(cuppingNote);

		if (result != 1) {
			log.error("[CuppingNote] 커핑 노트 저장 실패 - reviewId: {}, result: {}", cuppingNote.getReviewId(), result);
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}

		log.debug("[CuppingNote] 커핑 노트 저장 완료 - id: {}, reviewId: {}", cuppingNote.getId(),
			cuppingNote.getReviewId());
		return result;
	}

	@Override
	public int update(final CuppingNote cuppingNote) {
		final int result = commandMapper.update(cuppingNote);

		if (result != 1) {
			log.error("[CuppingNote] 커핑 노트 업데이트 실패 - id: {}, reviewId: {}, result: {}",
				cuppingNote.getId(), cuppingNote.getReviewId(), result);
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}

		log.debug("[CuppingNote] 커핑 노트 업데이트 완료 - id: {}, reviewId: {}", cuppingNote.getId(),
			cuppingNote.getReviewId());
		return result;
	}
}
