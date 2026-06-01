package com.backend.domain.review.service.query.implement;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.review.entity.Review;
import com.backend.domain.review.mapper.query.ReviewQueryMapper;
import com.backend.domain.review.service.query.ReviewQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewQueryServiceImpl implements ReviewQueryService {
	private final ReviewQueryMapper queryMapper;

	@Override
	public Review findById(final Long reviewId) {
		return queryMapper.findById(reviewId)
			.orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
	}

	@Override
	public List<Review> findAllByUserId(final Long userId, final Pageable pageable) {
		List<Review> list = queryMapper.findAllByUserId(userId, pageable);
		log.info("[Visit] 사용자의 모든 리뷰 조회 size: {}", list.size());
		return list;
	}

	@Override
	public int countAllByUserId(final Long userId) {
		return queryMapper.countAllByUserId(userId);
	}

	@Override
	public List<Review> findAllByStoreId(final Long storeId, final Pageable pageable) {
		List<Review> list = queryMapper.findAllByStoreId(storeId, pageable);
		log.info("[Review] 가맹점의 모든 리뷰 조회 - storeId: {}, size: {}", storeId, list.size());
		return list;
	}

	@Override
	public int countAllByStoreId(final Long storeId) {
		return queryMapper.countAllByStoreId(storeId);
	}

	@Override
	public boolean existsByVisitId(final Long visitId) {
		return queryMapper.existsByVisitId(visitId);
	}
}
