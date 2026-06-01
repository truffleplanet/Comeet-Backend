package com.backend.domain.ownerapplication.mapper.query;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.backend.domain.ownerapplication.entity.OwnerApplication;
import com.backend.domain.ownerapplication.entity.OwnerApplicationStatus;

@Mapper
public interface OwnerApplicationQueryMapper {
	Optional<OwnerApplication> findById(@Param("applicationId") Long applicationId);

	int countPendingByUserId(@Param("userId") Long userId);

	List<OwnerApplication> findAllByStatus(@Param("status") OwnerApplicationStatus status);
}
