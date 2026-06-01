package com.backend.domain.ai.factory;

import org.springframework.stereotype.Component;

import com.backend.domain.ai.constants.ImageGenerationConstants;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.ImageConfig;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GeminiConfigFactory {

	public GenerateContentConfig createImageGenerationConfig() {
		return GenerateContentConfig.builder()
			.responseModalities(
				ImageGenerationConstants.MODALITY_IMAGE,
				ImageGenerationConstants.MODALITY_TEXT
			)
			.imageConfig(createImageConfig())
			.build();
	}

	private ImageConfig createImageConfig() {
		return ImageConfig.builder()
			.imageSize(ImageGenerationConstants.IMAGE_SIZE_1K)
			.build();
	}
}
