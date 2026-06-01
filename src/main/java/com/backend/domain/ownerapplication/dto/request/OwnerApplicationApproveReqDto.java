package com.backend.domain.ownerapplication.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "가맹점주 신청 승인 요청 DTO")
public record OwnerApplicationApproveReqDto(
	@Schema(description = "관리자 검토 메모", example = "사업자 정보와 매장 정보 확인 완료")
	@Size(max = 500, message = "검토 메모는 500자를 초과할 수 없습니다.")
	String reviewComment
) {
}
