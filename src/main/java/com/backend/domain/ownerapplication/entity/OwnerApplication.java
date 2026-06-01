package com.backend.domain.ownerapplication.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.backend.domain.ownerapplication.dto.request.OwnerApplicationCreateReqDto;
import com.backend.domain.store.dto.request.StoreCreateReqDto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OwnerApplication {
	private Long id;
	private Long userId;
	private Long roasteryId;
	private String storeName;
	private String storeDescription;
	private String storeAddress;
	private BigDecimal latitude;
	private BigDecimal longitude;
	private String businessRegistrationNumber;
	private String representativeName;
	private String businessLicenseUrl;
	private String openingHours;
	private String category;
	private String phoneNumber;
	private String thumbnailUrl;
	private OwnerApplicationStatus status;
	private String rejectReason;
	private Long reviewedBy;
	private LocalDateTime reviewedAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static OwnerApplication create(final Long userId, final OwnerApplicationCreateReqDto reqDto) {
		return OwnerApplication.builder()
			.userId(userId)
			.roasteryId(reqDto.roasteryId())
			.storeName(reqDto.name())
			.storeDescription(reqDto.description())
			.storeAddress(reqDto.address())
			.latitude(reqDto.latitude())
			.longitude(reqDto.longitude())
			.businessRegistrationNumber(reqDto.businessRegistrationNumber())
			.representativeName(reqDto.representativeName())
			.businessLicenseUrl(reqDto.businessLicenseUrl())
			.openingHours(reqDto.openingHours())
			.category(reqDto.category())
			.phoneNumber(reqDto.phoneNumber())
			.thumbnailUrl(reqDto.thumbnailUrl())
			.status(OwnerApplicationStatus.PENDING)
			.build();
	}

	public StoreCreateReqDto toStoreCreateReqDto() {
		return new StoreCreateReqDto(
			roasteryId,
			storeName,
			storeDescription,
			storeAddress,
			latitude,
			longitude,
			openingHours,
			category,
			phoneNumber,
			thumbnailUrl
		);
	}
}
