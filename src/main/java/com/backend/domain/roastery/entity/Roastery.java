package com.backend.domain.roastery.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "roasteries")
public class Roastery {
	@Id
	private Long id;
	private String name;
	private String logoUrl;
	private String websiteUrl;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
