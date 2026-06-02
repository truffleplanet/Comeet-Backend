package com.backend.domain.store.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "가맹점 지도 목록 응답 DTO")
public record StoreResDto(
	@Schema(description = "매장 고유 ID", example = "1")
	Long id,

	@Schema(description = "매장명", example = "블루보틀 강남점")
	String name,

	@Schema(description = "매장 주소", example = "서울특별시 강남구 테헤란로 152")
	String address,

	@Schema(description = "매장 위도 좌표", example = "37.4995")
	BigDecimal latitude,

	@Schema(description = "매장 경도 좌표", example = "127.0288")
	BigDecimal longitude,

	@Schema(description = "매장 카테고리", example = "핸드드립", nullable = true)
	String category,

	@Schema(description = "평균 평점 (0.0 ~ 5.0)", example = "4.8")
	BigDecimal averageRating,

	@Schema(description = "총 리뷰 수", example = "234")
	Integer reviewCount,

	@Schema(description = "총 방문 수", example = "891")
	Integer visitCount,

	@Schema(description = "영업 중지 여부 (true: 영업 안함, false: 영업 중)", example = "false")
	boolean isClosed,

	@Schema(description = "사용자 위치로부터의 직선 거리 (미터 단위)", example = "250.5")
	Double distance
) {
}
