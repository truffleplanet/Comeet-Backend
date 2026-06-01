package com.backend.domain.review.mapper.query;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

import com.backend.domain.review.entity.Review;

@Mapper
public interface ReviewQueryMapper {
	Optional<Review> findById(@Param("reviewId") Long reviewId);

	List<Review> findAllByUserId(@Param("userId") Long userId, @Param("pageable") Pageable pageable);

	int countAllByUserId(@Param("userId") Long userId);

	List<Review> findAllByStoreId(@Param("storeId") Long storeId, @Param("pageable") Pageable pageable);

	int countAllByStoreId(@Param("storeId") Long storeId);

	boolean existsByVisitId(@Param("visitId") Long visitId);
}
