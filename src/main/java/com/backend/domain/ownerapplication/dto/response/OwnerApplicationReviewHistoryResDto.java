package com.backend.domain.ownerapplication.dto.response;

import java.time.LocalDateTime;

import com.backend.domain.ownerapplication.entity.OwnerApplicationReviewHistory;
import com.backend.domain.ownerapplication.entity.OwnerApplicationStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "가맹점주 신청 관리자 검토 이력 응답 DTO")
public record OwnerApplicationReviewHistoryResDto(
	Long id,
	Long applicationId,
	Long reviewerId,
	OwnerApplicationStatus status,
	String comment,
	LocalDateTime createdAt
) {
	public static OwnerApplicationReviewHistoryResDto from(final OwnerApplicationReviewHistory history) {
		return OwnerApplicationReviewHistoryResDto.builder()
			.id(history.getId())
			.applicationId(history.getApplicationId())
			.reviewerId(history.getReviewerId())
			.status(history.getStatus())
			.comment(history.getComment())
			.createdAt(history.getCreatedAt())
			.build();
	}
}
