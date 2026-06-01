package com.backend.domain.ownerapplication.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "가맹점주 신청 요청 DTO")
public record OwnerApplicationCreateReqDto(
	@Schema(description = "로스터리 ID", example = "1", requiredMode = RequiredMode.REQUIRED)
	@NotNull(message = "로스터리 ID는 필수 입력값입니다.")
	Long roasteryId,

	@Schema(description = "가맹점 이름", example = "카페 코밋", requiredMode = RequiredMode.REQUIRED)
	@NotBlank(message = "가맹점 이름은 필수 입력값입니다.")
	@Size(max = 100, message = "가맹점 이름은 100자를 초과할 수 없습니다.")
	String name,

	@Schema(description = "가맹점 설명", example = "스페셜티 커피 전문점", requiredMode = RequiredMode.NOT_REQUIRED)
	String description,

	@Schema(description = "주소", example = "서울시 강남구 테헤란로 123", requiredMode = RequiredMode.REQUIRED)
	@NotBlank(message = "주소는 필수 입력값입니다.")
	String address,

	@Schema(description = "위도", example = "37.5012", requiredMode = RequiredMode.REQUIRED)
	@NotNull(message = "위도는 필수 입력값입니다.")
	@DecimalMin(value = "-90.0", message = "위도는 -90.0 이상이어야 합니다.")
	@DecimalMax(value = "90.0", message = "위도는 90.0 이하여야 합니다.")
	BigDecimal latitude,

	@Schema(description = "경도", example = "127.0396", requiredMode = RequiredMode.REQUIRED)
	@NotNull(message = "경도는 필수 입력값입니다.")
	@DecimalMin(value = "-180.0", message = "경도는 -180.0 이상이어야 합니다.")
	@DecimalMax(value = "180.0", message = "경도는 180.0 이하여야 합니다.")
	BigDecimal longitude,

	@Schema(description = "영업시간", example = "09:00-22:00", requiredMode = RequiredMode.NOT_REQUIRED)
	@Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d-([01]\\d|2[0-3]):[0-5]\\d$", message = "영업시간은 HH:mm-HH:mm 형식이어야 합니다.")
	String openingHours,

	@Schema(description = "카테고리", example = "SPECIALTY_COFFEE", requiredMode = RequiredMode.NOT_REQUIRED)
	String category,

	@Schema(description = "전화번호", example = "02-1234-5678", requiredMode = RequiredMode.NOT_REQUIRED)
	String phoneNumber,

	@Schema(description = "썸네일 URL", example = "https://example.com/thumbnail.jpg", requiredMode = RequiredMode.NOT_REQUIRED)
	String thumbnailUrl
) {
}
