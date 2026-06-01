package com.backend.domain.ownerapplication.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.backend.domain.ownerapplication.entity.OwnerApplication;
import com.backend.domain.ownerapplication.entity.OwnerApplicationStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "가맹점주 신청 응답 DTO")
public record OwnerApplicationResDto(
	Long id,
	Long userId,
	Long roasteryId,
	String name,
	String description,
	String address,
	BigDecimal latitude,
	BigDecimal longitude,
	String openingHours,
	String category,
	String phoneNumber,
	String thumbnailUrl,
	OwnerApplicationStatus status,
	String rejectReason,
	Long reviewedBy,
	LocalDateTime reviewedAt,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static OwnerApplicationResDto from(final OwnerApplication application) {
		return OwnerApplicationResDto.builder()
			.id(application.getId())
			.userId(application.getUserId())
			.roasteryId(application.getRoasteryId())
			.name(application.getStoreName())
			.description(application.getStoreDescription())
			.address(application.getStoreAddress())
			.latitude(application.getLatitude())
			.longitude(application.getLongitude())
			.openingHours(application.getOpeningHours())
			.category(application.getCategory())
			.phoneNumber(application.getPhoneNumber())
			.thumbnailUrl(application.getThumbnailUrl())
			.status(application.getStatus())
			.rejectReason(application.getRejectReason())
			.reviewedBy(application.getReviewedBy())
			.reviewedAt(application.getReviewedAt())
			.createdAt(application.getCreatedAt())
			.updatedAt(application.getUpdatedAt())
			.build();
	}
}
