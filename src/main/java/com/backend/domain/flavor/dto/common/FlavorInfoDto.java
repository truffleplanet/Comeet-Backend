package com.backend.domain.flavor.dto.common;

import lombok.Builder;

@Builder
public record FlavorInfoDto(
	Long id,
	String code,
	Long parentId,
	Integer level,
	String path,
	String name,
	String description,
	String colorHex
) {
}
