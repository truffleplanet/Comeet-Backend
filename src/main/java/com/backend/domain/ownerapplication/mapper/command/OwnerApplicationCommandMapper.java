package com.backend.domain.ownerapplication.mapper.command;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.backend.domain.ownerapplication.entity.OwnerApplication;

@Mapper
public interface OwnerApplicationCommandMapper {
	void save(@Param("application") OwnerApplication application);

	int approve(@Param("applicationId") Long applicationId, @Param("adminId") Long adminId);

	int reject(
		@Param("applicationId") Long applicationId,
		@Param("adminId") Long adminId,
		@Param("rejectReason") String rejectReason
	);
}
