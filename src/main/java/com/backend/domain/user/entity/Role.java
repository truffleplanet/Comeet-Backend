package com.backend.domain.user.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Role {
	USER("ROLE_USER", "사용자", false),
	MANAGER("ROLE_MANAGER", "가맹점주", true),
	ADMIN("ROLE_ADMIN", "운영자", false),
	GUEST("ROLE_GUEST", "임시 사용자", false),
	WITHDRAWN("ROLE_WITHDRAWN", "탈퇴한 사용자", false),
	;

	private final String key;
	private final String description;
	private final boolean manager;

	public static boolean isNotActiveUser(final Role role) {
		return role == GUEST || role == WITHDRAWN;
	}

	public static boolean isManager(final Role role) {
		return role.manager;
	}

}
