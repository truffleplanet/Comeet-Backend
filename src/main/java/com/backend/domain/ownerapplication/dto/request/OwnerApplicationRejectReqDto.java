package com.backend.domain.ownerapplication.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "가맹점주 신청 거절 요청 DTO")
public record OwnerApplicationRejectReqDto(
	@Schema(description = "거절 사유", example = "사업자 정보 확인이 필요합니다.")
	@NotBlank(message = "거절 사유는 필수 입력값입니다.")
	@Size(max = 500, message = "거절 사유는 500자를 초과할 수 없습니다.")
	String rejectReason
) {
}
