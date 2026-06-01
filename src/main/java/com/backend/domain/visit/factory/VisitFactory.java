package com.backend.domain.visit.factory;

import org.springframework.stereotype.Component;

import com.backend.domain.user.entity.User;
import com.backend.domain.visit.dto.request.VerifyReqDto;
import com.backend.domain.visit.entity.Visit;
import com.backend.domain.visit.validator.VisitValidator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VisitFactory {

	private final VisitValidator visitValidator;

	public Visit create(final User user, final VerifyReqDto reqDto, final Boolean isVerified) {
		Visit visit = Visit.builder()
			.userId(user.getId())
			.menuId(reqDto.menuId())
			.latitude(reqDto.userLocationDto().latitude())
			.longitude(reqDto.userLocationDto().longitude())
			.isVerified(isVerified)
			.build();

		visitValidator.validate(visit);

		return visit;
	}
}
