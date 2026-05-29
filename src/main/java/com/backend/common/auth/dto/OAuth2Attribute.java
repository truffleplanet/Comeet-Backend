package com.backend.common.auth.dto;

import java.util.Map;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.user.entity.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuth2Attribute {
	public static final String NAVER = "naver";
	private Map<String, Object> attributes;
	private String name;
	private String email;
	private String profileUrl;
	private String providerId;

	public static OAuth2Attribute of(final String registrationId, final Map<String, Object> attributes) {
		return switch (registrationId) {
			case NAVER -> ofNaver(attributes);
			default -> throw new BusinessException(ErrorCode.INVALID_INPUT);
		};
	}

	private static OAuth2Attribute ofNaver(Map<String, Object> attributes) {
		Map<String, Object> naverAttributes = (Map<String, Object>)attributes.get("response");

		String name = extractString(naverAttributes, "name");
		String email = extractString(naverAttributes, "email");
		String providerId = extractString(naverAttributes, "id");
		String profileImage = extractString(naverAttributes, "profile_image");

		return OAuth2Attribute.builder()
			.name(name)
			.email(email)
			.providerId(providerId)
			.profileUrl(profileImage)
			.attributes(naverAttributes)
			.build();

	}

	public User toEntity(String socialId, String profileImageUrl) {
		return User.of(name, email, profileImageUrl, socialId);
	}

	private static String extractString(Map<String, Object> attributes, String key) {
		Object value = attributes.get(key);
		return value != null ? value.toString() : null;
	}
}
