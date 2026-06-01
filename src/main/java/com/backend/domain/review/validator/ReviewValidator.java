package com.backend.domain.review.validator;

import org.springframework.stereotype.Component;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.validator.Validator;
import com.backend.domain.review.entity.Review;

@Component
public class ReviewValidator implements Validator<Review> {
	@Override
	public void validate(final Review review) {
		validateNotNull(review);
	}

	private void validateNotNull(final Review review) {
		if (review == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}

	public void validateReviewBelongsToUser(final Review review, final Long userId) {
		if (!review.getUserId().equals(userId)) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}
	}

	public void validateReviewNotDeleted(final Review review) {
		if (review.getDeletedAt() != null) {
			throw new BusinessException(ErrorCode.ALREADY_DELETED_REVIEW);
		}
	}
}
