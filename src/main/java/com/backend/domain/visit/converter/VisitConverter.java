package com.backend.domain.visit.converter;

import com.backend.domain.visit.dto.common.VisitInfoDto;
import com.backend.domain.visit.dto.common.VisitPageDto;
import com.backend.domain.visit.dto.response.VerifiedResDto;
import com.backend.domain.visit.entity.Visit;

import lombok.experimental.UtilityClass;

@UtilityClass
public class VisitConverter {

	public VerifiedResDto toVerifiedResDto(final Visit visit) {
		return VerifiedResDto.builder()
			.visitId(visit.getId())
			.isVerified(visit.getIsVerified())
			.build();
	}

	public VisitInfoDto toVisitInfoDto(final Visit visit) {
		return VisitInfoDto.builder()
			.visitId(visit.getId())
			.menuId(visit.getMenuId())
			.latitude(visit.getLatitude())
			.longitude(visit.getLongitude())
			.verified(visit.getIsVerified())
			.visitedAt(visit.getCreatedAt())
			.build();

	}

	public static VisitPageDto toVisitPageDto(final Visit visit) {
		return VisitPageDto.builder()
			.visitId(visit.getId())
			.menuId(visit.getMenuId())
			.verified(visit.getIsVerified())
			.visitedAt(visit.getCreatedAt())
			.build();
	}
}
