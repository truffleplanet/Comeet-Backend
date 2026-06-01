package com.backend.domain.store.mapper.command;

import java.math.BigDecimal;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.backend.domain.store.entity.Store;

@Mapper
public interface StoreCommandMapper {

	void save(@Param("store") Store store);

	int update(@Param("store") Store store);

	int softDelete(@Param("storeId") Long storeId);

	int applyReviewStatsDelta(
		@Param("storeId") Long storeId,
		@Param("reviewCountDelta") int reviewCountDelta,
		@Param("ratingCountDelta") int ratingCountDelta,
		@Param("ratingSumDelta") BigDecimal ratingSumDelta
	);

	int incrementVisitCount(@Param("storeId") Long storeId);
}
