package com.backend.domain.ownerapplication.service.command;

import com.backend.domain.ownerapplication.entity.OwnerApplication;

public interface OwnerApplicationCommandService {
	OwnerApplication save(OwnerApplication application);

	void approve(Long applicationId, Long adminId);

	void reject(Long applicationId, Long adminId, String rejectReason);
}
