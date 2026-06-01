package com.backend.domain.ai.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageGenerationConstants {

	public static final String MODALITY_IMAGE = "IMAGE";
	public static final String MODALITY_TEXT = "TEXT";
	public static final String IMAGE_SIZE_1K = "1K";
	public static final String DEFAULT_MIME_TYPE = "image/png";
	public static final String USER_ROLE = "user";
}
