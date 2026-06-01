package com.backend.domain.ai.parser;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.ai.constants.ImageGenerationConstants;
import com.backend.domain.ai.dto.GeneratedImage;
import com.google.genai.ResponseStream;
import com.google.genai.types.Blob;
import com.google.genai.types.Candidate;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GeminiResponseParser {

	public GeneratedImage parseImageResponse(final ResponseStream<GenerateContentResponse> responseStream) {
		for (GenerateContentResponse response : responseStream) {
			Optional<GeneratedImage> imageOpt = extractImageFromResponse(response);
			if (imageOpt.isPresent()) {
				return imageOpt.get();
			}
		}

		log.error("[AI] 이미지 생성 실패 - 응답이 비어있음");
		throw new BusinessException(ErrorCode.AI_IMAGE_EMPTY_RESPONSE);
	}

	private Optional<GeneratedImage> extractImageFromResponse(final GenerateContentResponse response) {
		return response.candidates()
			.flatMap(candidates -> candidates.isEmpty() ? Optional.empty() : Optional.of(candidates.getFirst()))
			.flatMap(Candidate::content)
			.flatMap(Content::parts)
			.flatMap(this::extractImageFromParts);
	}

	private Optional<GeneratedImage> extractImageFromParts(final List<Part> parts) {
		return parts.stream()
			.map(Part::inlineData)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(this::createGeneratedImage)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.findFirst();
	}

	private Optional<GeneratedImage> createGeneratedImage(final Blob blob) {
		return blob.data()
			.map(data -> GeneratedImage.of(
				data,
				blob.mimeType().orElse(ImageGenerationConstants.DEFAULT_MIME_TYPE)
			))
			.or(() -> {
				log.warn("[AI] Blob 데이터가 비어있음");
				return Optional.empty();
			});
	}
}