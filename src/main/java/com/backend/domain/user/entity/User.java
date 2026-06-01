package com.backend.domain.user.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
	private Long id;
	private String name;
	private String email;
	private String password;
	private String nickname;
	private String profileImageUrl;
	private String socialId;
	private Role role;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static User of(
		final String name,
		final String email,
		final String profileImageUrl,
		final String socialId
	) {
		return User.builder()
			.name(name)
			.email(email)
			.profileImageUrl(profileImageUrl)
			.password(generateTemporaryPassword())
			.socialId(socialId)
			.role(Role.GUEST)
			.build();
	}

	public static User local(
		final String name,
		final String email,
		final String password,
		final String nickname
	) {
		return User.builder()
			.name(name)
			.email(email)
			.password(password)
			.nickname(nickname)
			.socialId(email)
			.role(Role.USER)
			.build();
	}

	private static String generateTemporaryPassword() {
		return UUID.randomUUID().toString();
	}

	public User update(final String email, final String name) {
		this.email = email;
		this.name = name;
		return this;
	}

	public void updateRole(final Role role) {
		this.role = role;
	}
}
