package com.backend.domain.review.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.backend.domain.bean.enums.RoastingLevel;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "커핑 노트 응답 DTO")
public record CuppingResDto(
	@Schema(description = "커핑 노트 ID", example = "1")
	Long id,

	@Schema(description = "로스팅 레벨", example = "MEDIUM")
	RoastingLevel roastingLevel,

	@Schema(description = "Fragrance 점수", example = "8.50")
	BigDecimal fragranceScore,

	@Schema(description = "Aroma 점수", example = "8.25")
	BigDecimal aromaScore,

	@Schema(description = "Flavor 점수", example = "8.75")
	BigDecimal flavorScore,

	@Schema(description = "Aftertaste 점수", example = "8.00")
	BigDecimal aftertasteScore,

	@Schema(description = "Acidity 점수", example = "8.50")
	BigDecimal acidityScore,

	@Schema(description = "Sweetness 점수", example = "8.25")
	BigDecimal sweetnessScore,

	@Schema(description = "Mouthfeel 점수", example = "8.00")
	BigDecimal mouthfeelScore,

	@Schema(description = "총점 (자동 계산)", example = "58.25")
	BigDecimal totalScore,

	@Schema(description = "Fragrance/Aroma 상세 설명", example = "꽃향기와 베리류의 향이 강하게 느껴짐")
	String fragranceAromaDetail,

	@Schema(description = "Flavor/Aftertaste 상세 설명", example = "초콜릿과 카라멜의 풍미가 지속됨")
	String flavorAftertasteDetail,

	@Schema(description = "Acidity 노트", example = "밝고 청량한 산미")
	String acidityNotes,

	@Schema(description = "Sweetness 노트", example = "부드러운 단맛")
	String sweetnessNotes,

	@Schema(description = "Mouthfeel 노트", example = "크리미한 질감")
	String mouthfeelNotes,

	@Schema(description = "전체적인 노트", example = "균형 잡힌 풍미 프로파일")
	String overallNotes,

	@Schema(description = "생성일시", example = "2024-01-15T10:30:00")
	LocalDateTime createdAt,

	@Schema(description = "수정일시", example = "2024-01-15T14:30:00")
	LocalDateTime updatedAt
) implements Serializable {
}
