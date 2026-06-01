package com.backend.domain.user.dto.request;

import org.hibernate.validator.constraints.Length;

import com.backend.domain.user.entity.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "사용자 서비스 등록 요청 DTO")
public record UserRegisterReqDto(
	@Schema(
		description = "사용자 닉네임 (1~12자, 한글/영문만 허용)",
		example = "김싸피",
		minLength = 1,
		maxLength = 12
	)
	@NotBlank(message = "닉네임은 공백일 수 없습니다.")
	@Length(min = 1, max = 12, message = "닉네임은 1자 이상 12자 이하여야 합니다.")
	String nickname,

	@Schema(
		description = "사용자 역할 (USER만 허용, 가맹점주는 별도 신청 후 승인)",
		example = "USER",
		allowableValues = {"USER"}
	)
	@NotNull(message = "역할은 필수 입력값입니다.")
	Role role
) {
}
