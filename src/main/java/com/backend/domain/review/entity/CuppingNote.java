package com.backend.domain.review.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.backend.domain.bean.enums.RoastingLevel;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "cupping_notes")
public class CuppingNote {
	@Id
	private Long id;
	private Long reviewId;
	private RoastingLevel roastingLevel;
	private BigDecimal fragranceScore;
	private BigDecimal aromaScore;
	private BigDecimal flavorScore;
	private BigDecimal aftertasteScore;
	private BigDecimal acidityScore;
	private BigDecimal sweetnessScore;
	private BigDecimal mouthfeelScore;
	private BigDecimal totalScore;
	private String fragranceAromaDetail;
	private String flavorAftertasteDetail;
	private String acidityNotes;
	private String sweetnessNotes;
	private String mouthfeelNotes;
	private String overallNotes;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
