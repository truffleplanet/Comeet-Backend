package com.backend.common.auth.principal;

import com.backend.domain.user.entity.Role;
import com.backend.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultAuthenticatedUser implements AuthenticatedUser {

	private final User user;

	@Override
	public String getId() {
		return user.getSocialId();
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public Role getRole() {
		return user.getRole();
	}
}
