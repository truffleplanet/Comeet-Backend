package com.backend.domain.user.dto.request;

import com.backend.domain.user.entity.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "사용자 역할 변경 요청 DTO")
public record UserRoleUpdateReqDto(
	@Schema(
		description = "변경할 역할 (일반 사용자의 MANAGER 승격은 허용하지 않습니다)",
		example = "USER",
		allowableValues = {"USER", "MANAGER"}
	)
	@NotNull(message = "역할은 필수 입력값입니다.")
	Role role
) {
}
