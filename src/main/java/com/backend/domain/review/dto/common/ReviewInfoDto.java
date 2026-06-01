package com.backend.domain.review.dto.common;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "리뷰 상세 정보 DTO")
public record ReviewInfoDto(
	@Schema(description = "리뷰 ID", example = "1")
	Long reviewId,

	@Schema(description = "사용자 ID", example = "1")
	Long userId,

	@Schema(description = "가맹점 ID", example = "1")
	Long storeId,

	@Schema(description = "메뉴 ID", example = "1")
	Long menuId,

	@Schema(description = "방문 인증 ID", example = "1")
	Long visitId,

	@Schema(description = "리뷰 내용", example = "산미가 강하고 과일향이 풍부한 원두였습니다.")
	String content,

	@Schema(description = "리뷰 이미지 URL", example = "https://example.com/image.jpg")
	String imageUrl,

	@Schema(description = "공개 여부", example = "true")
	Boolean isPublic,

	@Schema(description = "평점 (0.5 ~ 5.0)", example = "4.5")
	BigDecimal rating,

	@Schema(description = "생성일시", example = "2025-12-09T12:00:00")
	LocalDateTime createdAt
) {
}
