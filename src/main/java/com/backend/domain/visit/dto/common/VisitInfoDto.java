package com.backend.domain.visit.dto.common;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "방문 인증 상세 정보 DTO")
public record VisitInfoDto(
	@Schema(description = "방문 인증 ID", example = "1")
	Long visitId,

	@Schema(description = "인증한 메뉴 ID", example = "10")
	Long menuId,

	@Schema(description = "인증 시 위도 좌표", example = "37.5665")
	Double latitude,

	@Schema(description = "인증 시 경도 좌표", example = "126.9780")
	Double longitude,

	@Schema(description = "인증 성공 여부", example = "true")
	Boolean verified,

	@Schema(description = "방문 인증 일시", example = "2024-12-07T14:30:00")
	LocalDateTime visitedAt
) {
}
