package com.backend.domain.ownerapplication.mapper.command;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.backend.domain.ownerapplication.entity.OwnerApplication;
import com.backend.domain.ownerapplication.entity.OwnerApplicationStatus;

@Mapper
public interface OwnerApplicationCommandMapper {
	void save(@Param("application") OwnerApplication application);

	int approve(@Param("applicationId") Long applicationId, @Param("adminId") Long adminId);

	int reject(
		@Param("applicationId") Long applicationId,
		@Param("adminId") Long adminId,
		@Param("rejectReason") String rejectReason
	);

	void saveReviewHistory(
		@Param("applicationId") Long applicationId,
		@Param("reviewerId") Long reviewerId,
		@Param("status") OwnerApplicationStatus status,
		@Param("comment") String comment
	);
}
