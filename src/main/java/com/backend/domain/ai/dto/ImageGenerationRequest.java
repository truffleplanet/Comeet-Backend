package com.backend.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이미지 생성 요청 DTO")
public record ImageGenerationRequest(
	@Schema(description = "이미지 생성 프롬프트", example = "A beautiful coffee passport image", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "프롬프트는 필수 입력값입니다.")
	String prompt
) {
}
