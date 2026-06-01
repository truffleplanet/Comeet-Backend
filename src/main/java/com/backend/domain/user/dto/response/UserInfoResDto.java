package com.backend.domain.user.dto.response;

import com.backend.domain.user.entity.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "유저 정보 응답 DTO")
public record UserInfoResDto(
	@Schema(description = "유저 ID", example = "1")
	Long userId,

	@Schema(description = "유저 이름", example = "김싸피")
	String name,

	@Schema(description = "이메일", example = "user@example.com")
	String email,

	@Schema(description = "닉네임", example = "싸피")
	String nickname,

	@Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
	String profileImageUrl,

	@Schema(description = "역할", example = "USER")
	Role role
) {
}
