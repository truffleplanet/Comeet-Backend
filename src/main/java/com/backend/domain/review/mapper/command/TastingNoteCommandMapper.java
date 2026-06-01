package com.backend.domain.review.mapper.command;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TastingNoteCommandMapper {
	int insertTastingNotes(@Param("reviewId") Long reviewId, @Param("flavorIdList") List<Long> flavorIdList);

	void deleteAllByReviewId(@Param("reviewId") Long reviewId);
}
