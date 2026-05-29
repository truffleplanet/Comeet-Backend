package com.backend.domain.ai.service;

import org.springframework.stereotype.Service;

import com.backend.common.config.property.GeminiProperty;
import com.backend.common.enums.FileType;
import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.s3.service.S3FileUploader;
import com.backend.domain.ai.dto.GeneratedImage;
import com.backend.domain.ai.dto.ImageGenerationRequest;
import com.backend.domain.ai.factory.GeminiConfigFactory;
import com.backend.domain.ai.parser.GeminiResponseParser;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class GeminiImageGenerationService implements ImageGenerationService {

	private final Client client;
	private final GeminiProperty geminiProperty;
	private final S3FileUploader s3FileUploader;
	private final GeminiConfigFactory configFactory;
	private final GeminiResponseParser responseParser;

	@Override
	public String generate(final String prompt) {
		ImageGenerationRequest request = new ImageGenerationRequest(prompt);
		GeneratedImage generatedImage = generateImage(request);
		return uploadToS3(generatedImage);
	}

	private GeneratedImage generateImage(final ImageGenerationRequest request) {
		log.info("[AI] 이미지 생성 요청 - model: {}, prompt: {}", geminiProperty.model(), request.prompt());

		GenerateContentConfig config = configFactory.createImageGenerationConfig();
		Content content = createContent(request.prompt());

		try (ResponseStream<GenerateContentResponse> responseStream =
				 client.models.generateContentStream(geminiProperty.model(), content, config)) {
			return responseParser.parseImageResponse(responseStream);
		} catch (Exception e) {
			log.error("[AI] 이미지 생성 중 오류 발생 - prompt: {}", request.prompt(), e);
			throw new BusinessException(ErrorCode.AI_IMAGE_GENERATION_FAILED);
		}
	}

	private Content createContent(final String prompt) {
		return Content.builder()
			.role("user")
			.parts(Part.fromText(prompt))
			.build();
	}

	private String uploadToS3(final GeneratedImage generatedImage) {
		log.info("[AI] 이미지 생성 성공 - size: {} bytes, mimeType: {}",
			generatedImage.sizeInBytes(), generatedImage.mimeType());

		String imageUrl = s3FileUploader.uploadFile(generatedImage.data(), generatedImage.mimeType(), FileType.IMAGE);

		log.info("[AI] S3 업로드 완료 - url: {}", imageUrl);
		return imageUrl;
	}
}