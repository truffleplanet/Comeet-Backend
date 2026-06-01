package com.backend.domain.ownerapplication.entity;

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
public class OwnerApplicationReviewHistory {
	private Long id;
	private Long applicationId;
	private Long reviewerId;
	private OwnerApplicationStatus status;
	private String comment;
	private LocalDateTime createdAt;
}
