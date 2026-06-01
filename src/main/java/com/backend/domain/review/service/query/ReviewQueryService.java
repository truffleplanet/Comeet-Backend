package com.backend.domain.review.service.query;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.backend.domain.review.entity.Review;

public interface ReviewQueryService {
	Review findById(Long reviewId);

	List<Review> findAllByUserId(Long id, Pageable pageable);

	int countAllByUserId(Long id);

	List<Review> findAllByStoreId(Long storeId, Pageable pageable);

	int countAllByStoreId(Long storeId);

	boolean existsByVisitId(Long visitId);
}
