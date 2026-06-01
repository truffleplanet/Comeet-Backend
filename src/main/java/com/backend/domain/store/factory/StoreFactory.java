package com.backend.domain.store.factory;

import java.math.BigDecimal;
import java.time.LocalTime;

import org.springframework.stereotype.Component;

import com.backend.common.util.TimeUtils;
import com.backend.domain.store.dto.request.StoreCreateReqDto;
import com.backend.domain.store.dto.request.StoreUpdateReqDto;
import com.backend.domain.store.entity.Store;
import com.backend.domain.store.validator.StoreValidator;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreFactory {

	private static final BigDecimal INITIAL_AVERAGE_RATING = BigDecimal.ZERO;
	private static final int INITIAL_COUNT = 0;

	private final StoreValidator storeValidator;

	public Store create(final StoreCreateReqDto reqDto, final Long ownerId) {
		LocalTime[] times = TimeUtils.parseOpeningHours(reqDto.openingHours());
		Store store = Store.builder()
			.ownerId(ownerId)
			.roasteryId(reqDto.roasteryId())
			.name(reqDto.name())
			.description(reqDto.description())
			.address(reqDto.address())
			.latitude(reqDto.latitude())
			.longitude(reqDto.longitude())
			.phoneNumber(reqDto.phoneNumber())
			.category(reqDto.category())
			.thumbnailUrl(reqDto.thumbnailUrl())
			.openTime(TimeUtils.getOpenTime(times))
			.closeTime(TimeUtils.getCloseTime(times))
			.averageRating(INITIAL_AVERAGE_RATING)
			.reviewCount(INITIAL_COUNT)
			.ratingCount(INITIAL_COUNT)
			.ratingSum(BigDecimal.ZERO)
			.visitCount(INITIAL_COUNT)
			.isClosed(false)
			.build();

		storeValidator.validate(store);
		return store;
	}

	public Store update(final Store store, final StoreUpdateReqDto reqDto) {
		Store.StoreBuilder builder = store.toBuilder();

		applyUpdateIfPresent(builder, reqDto);
		applyOpeningHoursIfPresent(builder, reqDto.openingHours());

		Store updatedStore = builder.build();
		storeValidator.validate(updatedStore);
		return updatedStore;
	}

	private void applyUpdateIfPresent(final Store.StoreBuilder builder, final StoreUpdateReqDto reqDto) {
		if (reqDto.name() != null) {
			builder.name(reqDto.name());
		}
		if (reqDto.description() != null) {
			builder.description(reqDto.description());
		}
		if (reqDto.address() != null) {
			builder.address(reqDto.address());
		}
		if (reqDto.latitude() != null) {
			builder.latitude(reqDto.latitude());
		}
		if (reqDto.longitude() != null) {
			builder.longitude(reqDto.longitude());
		}
		if (reqDto.phoneNumber() != null) {
			builder.phoneNumber(reqDto.phoneNumber());
		}
		if (reqDto.category() != null) {
			builder.category(reqDto.category());
		}
		if (reqDto.thumbnailUrl() != null) {
			builder.thumbnailUrl(reqDto.thumbnailUrl());
		}
		if (reqDto.isClosed() != null) {
			builder.isClosed(reqDto.isClosed());
		}
	}

	private void applyOpeningHoursIfPresent(final Store.StoreBuilder builder, final String openingHours) {
		if (openingHours == null) {
			return;
		}

		LocalTime[] times = TimeUtils.parseOpeningHours(openingHours);
		if (times != null) {
			builder.openTime(TimeUtils.getOpenTime(times))
				.closeTime(TimeUtils.getCloseTime(times));
		}
	}
}
