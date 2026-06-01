package com.backend.domain.ai.dto;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;

import lombok.Builder;

@Builder
public record GeneratedImage(
	byte[] data,
	String mimeType,
	int sizeInBytes
) {
	public GeneratedImage {
		if (data == null || data.length == 0) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		if (mimeType == null || mimeType.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}

	public static GeneratedImage of(final byte[] data, final String mimeType) {
		return GeneratedImage.builder()
			.data(data)
			.mimeType(mimeType)
			.sizeInBytes(data.length)
			.build();
	}
}
