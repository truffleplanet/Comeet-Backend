package com.backend.domain.review.service.command.implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.domain.review.entity.Review;
import com.backend.domain.review.mapper.command.ReviewCommandMapper;
import com.backend.domain.review.service.command.ReviewCommandService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewCommandServiceImpl implements ReviewCommandService {
	private final ReviewCommandMapper commandMapper;

	@Override
	public int insert(final Review review) {
		int result = commandMapper.insert(review);
		log.info("[Review] 리뷰 저장 완료 - id: {}", review.getId());
		return result;
	}

	@Override
	public int update(final Review review) {
		int result = commandMapper.update(review);
		log.info("[Review] 리뷰 업데이트 완료 - id: {}", review.getId());
		return result;
	}

	@Override
	public int softDelete(final Long reviewId) {
		int result = commandMapper.softDelete(reviewId);
		log.info("[Review] 리뷰 삭제 완료 - id: {}", reviewId);
		return result;
	}
}
