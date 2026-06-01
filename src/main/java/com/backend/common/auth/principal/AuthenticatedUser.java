package com.backend.common.auth.principal;

import com.backend.domain.user.entity.Role;
import com.backend.domain.user.entity.User;

public interface AuthenticatedUser {
	String getId();

	User getUser();

	Role getRole();
}
