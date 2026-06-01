package com.backend.common.auth.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoleAuthority {
	public static final String ADMIN = "hasAuthority('ROLE_ADMIN')";
	public static final String OWNER = "hasAuthority('ROLE_OWNER')";
}
