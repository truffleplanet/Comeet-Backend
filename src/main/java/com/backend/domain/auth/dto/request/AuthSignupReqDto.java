package com.backend.domain.auth.dto.request;

import org.hibernate.validator.constraints.Length;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청 DTO")
public record AuthSignupReqDto(
	@Schema(description = "이름", example = "김싸피")
	@NotBlank(message = "이름은 필수 입력값입니다.")
	String name,

	@Schema(description = "이메일", example = "user@example.com")
	@NotBlank(message = "이메일은 필수 입력값입니다.")
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	String email,

	@Schema(description = "비밀번호", example = "password1234", minLength = 8)
	@NotBlank(message = "비밀번호는 필수 입력값입니다.")
	@Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
	String password,

	@Schema(description = "닉네임", example = "김싸피", minLength = 1, maxLength = 12)
	@NotBlank(message = "닉네임은 필수 입력값입니다.")
	@Length(min = 1, max = 12, message = "닉네임은 1자 이상 12자 이하여야 합니다.")
	String nickname
) {
}
