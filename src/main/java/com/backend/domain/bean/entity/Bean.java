package com.backend.domain.bean.entity;

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
@Table(name = "beans")
public class Bean {
	@Id
	private Long id;
	private Long roasteryId;
	private String name;
	private String country;
	private String farm;
	private String variety;
	private String processingMethod;
	private RoastingLevel roastingLevel;
	private LocalDateTime deletedAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
