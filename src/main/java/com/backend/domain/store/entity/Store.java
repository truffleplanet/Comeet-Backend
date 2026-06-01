package com.backend.domain.store.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Store {
	private Long id;
	private Long roasteryId;
	private Long ownerId;
	private String name;
	private String description;
	private String address;
	private BigDecimal latitude;
	private BigDecimal longitude;
	private String phoneNumber;
	private String category;
	private String thumbnailUrl;
	private LocalTime openTime;
	private LocalTime closeTime;
	private BigDecimal averageRating;
	private Integer reviewCount;
	private Integer ratingCount;
	private BigDecimal ratingSum;
	private Integer visitCount;
	private boolean isClosed;
	private LocalDateTime deletedAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
