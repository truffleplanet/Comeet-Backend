package com.backend.domain.recommendation.service.query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.backend.common.util.GeoUtils;
import com.backend.domain.recommendation.dto.internal.MenuWithBeanScoreDto;

public interface RecommendationQueryService {

	List<MenuWithBeanScoreDto> findFilteredMenus(
		List<String> dislikedTags,
		List<String> preferredRoastLevels,
		GeoUtils.BoundingBox boundingBox
	);

	List<MenuWithBeanScoreDto> findMenusByBeanId(Long beanId, GeoUtils.BoundingBox boundingBox);

	Optional<Double> calculateDistance(MenuWithBeanScoreDto menu, BigDecimal latitude, BigDecimal longitude);
}
