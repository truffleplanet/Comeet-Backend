package com.backend.domain.store.converter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.backend.common.util.GeoUtils;
import com.backend.common.util.TimeUtils;
import com.backend.domain.store.dto.response.StoreDetailResDto;
import com.backend.domain.store.dto.response.StoreListResDto;
import com.backend.domain.store.dto.response.StoreResDto;
import com.backend.domain.store.entity.Store;
import com.backend.domain.store.vo.StoreSearchBoundsVo;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StoreConverter {

	private static final double SEARCH_BOUND_MARGIN_DEGREES = 0.001;

	public static StoreDetailResDto toStoreDetailResponse(final Store store) {
		return StoreDetailResDto.builder()
			.id(store.getId())
			.roasteryId(store.getRoasteryId())
			.name(store.getName())
			.description(store.getDescription())
			.address(store.getAddress())
			.latitude(store.getLatitude())
			.longitude(store.getLongitude())
			.phoneNumber(store.getPhoneNumber())
			.category(store.getCategory())
			.thumbnailUrl(store.getThumbnailUrl())
			.openTime(store.getOpenTime())
			.closeTime(store.getCloseTime())
			.openingHours(TimeUtils.formatOpeningHours(store.getOpenTime(), store.getCloseTime()).orElse(null))
			.averageRating(store.getAverageRating())
			.reviewCount(store.getReviewCount())
			.visitCount(store.getVisitCount())
			.isClosed(store.isClosed())
			.build();
	}

	public static StoreResDto toStoreResponse(final Store store, final Double distanceKm) {
		return StoreResDto.builder()
			.id(store.getId())
			.name(store.getName())
			.address(store.getAddress())
			.latitude(store.getLatitude())
			.longitude(store.getLongitude())
			.category(store.getCategory())
			.averageRating(store.getAverageRating())
			.reviewCount(store.getReviewCount())
			.visitCount(store.getVisitCount())
			.isClosed(store.isClosed())
			.distance(GeoUtils.convertKmToMeters(distanceKm))
			.build();
	}

	public static StoreListResDto toStoreListResponse(final List<Store> stores, final Map<Long, Double> distanceMap) {
		List<StoreResDto> storeList = stores.stream()
			.map(store -> toStoreResponse(store, distanceMap.get(store.getId())))
			.toList();

		return StoreListResDto.builder()
			.totalCount(storeList.size())
			.stores(storeList)
			.build();
	}

	public static StoreSearchBoundsVo toStoreSearchBoundsVo(final Store store) {
		return StoreSearchBoundsVo.builder()
			.minLatitude(store.getLatitude().subtract(BigDecimal.valueOf(SEARCH_BOUND_MARGIN_DEGREES)))
			.maxLatitude(store.getLatitude().add(BigDecimal.valueOf(SEARCH_BOUND_MARGIN_DEGREES)))
			.minLongitude(store.getLongitude().subtract(BigDecimal.valueOf(SEARCH_BOUND_MARGIN_DEGREES)))
			.maxLongitude(store.getLongitude().add(BigDecimal.valueOf(SEARCH_BOUND_MARGIN_DEGREES)))
			.build();
	}

	public static List<StoreDetailResDto> toStoreDetailResponseList(final List<Store> stores) {
		return stores.stream()
			.map(StoreConverter::toStoreDetailResponse)
			.toList();
	}
}
