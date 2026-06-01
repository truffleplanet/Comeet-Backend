package com.backend.domain.visit.entity;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Visit {
	private Long id;
	private Long userId;
	private Long menuId;
	private Double latitude;
	private Double longitude;
	private Boolean isVerified;
	private LocalDateTime createdAt;

	public static Visit insertOf() {
		return Visit.builder()
			.build();
	}

	public static Visit updateOf() {
		return Visit.builder()
			.build();
	}
}
