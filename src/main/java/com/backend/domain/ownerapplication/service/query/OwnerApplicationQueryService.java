package com.backend.domain.ownerapplication.service.query;

import java.util.List;

import com.backend.domain.ownerapplication.entity.OwnerApplication;
import com.backend.domain.ownerapplication.entity.OwnerApplicationReviewHistory;
import com.backend.domain.ownerapplication.entity.OwnerApplicationStatus;

public interface OwnerApplicationQueryService {
	OwnerApplication findById(Long applicationId);

	OwnerApplication findLatestByUserId(Long userId);

	boolean existsPendingByUserId(Long userId);

	List<OwnerApplication> findAllByStatus(OwnerApplicationStatus status);

	List<OwnerApplicationReviewHistory> findReviewHistoriesByApplicationId(Long applicationId);
}
