package com.backend.domain.roastery.validator;

import org.springframework.stereotype.Component;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.validator.Validator;
import com.backend.domain.roastery.entity.Roastery;

@Component
public class RoasteryValidator implements Validator<Roastery> {
	@Override
	public void validate(final Roastery roastery) {
		validateNotNull(roastery);
		validateName(roastery.getName());
	}

	private void validateNotNull(final Roastery roastery) {
		if (roastery == null) {
			throw new BusinessException(ErrorCode.INVALID_ROASTERY_REQUEST);
		}
	}

	private void validateName(final String name) {
		if (name == null || name.isBlank()) {
			throw new BusinessException(ErrorCode.ROASTERY_NAME_REQUIRED);
		}
	}
}
