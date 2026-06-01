package com.backend.domain.visit.validator;

import org.springframework.stereotype.Component;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.validator.Validator;
import com.backend.domain.visit.entity.Visit;

@Component
public class VisitValidator implements Validator<Visit> {

	private static final double KOREA_MIN_LATITUDE = 33.0;
	private static final double KOREA_MAX_LATITUDE = 38.6;
	private static final double KOREA_MIN_LONGITUDE = 124.0;
	private static final double KOREA_MAX_LONGITUDE = 131.9;

	@Override
	public void validate(final Visit visit) {
		validateNotNull(visit);
		validateKoreaBoundary(visit);
	}

	private void validateNotNull(final Visit visit) {
		if (visit == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}

	private void validateKoreaBoundary(final Visit visit) {
		Double latitude = visit.getLatitude();
		Double longitude = visit.getLongitude();

		if (latitude == null || longitude == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}

		if (isOutOfKorea(latitude, longitude)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}

	private boolean isOutOfKorea(final Double latitude, final Double longitude) {
		return latitude < KOREA_MIN_LATITUDE || latitude > KOREA_MAX_LATITUDE
			|| longitude < KOREA_MIN_LONGITUDE || longitude > KOREA_MAX_LONGITUDE;
	}

	public void validateVisitBelongsToUser(final Long visitUserId, final Long userId) {
		if (!visitUserId.equals(userId)) {
			throw new BusinessException(ErrorCode.VISIT_NOT_BELONG_TO_USER);
		}
	}
}

