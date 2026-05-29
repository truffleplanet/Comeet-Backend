package com.backend.domain.store.validator;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.validator.Validator;
import com.backend.domain.store.entity.Store;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StoreValidator implements Validator<Store> {

	@Override
	public void validate(final Store store) {
		validateStoreExists(store, ErrorCode.BAD_REQUEST);
		validateName(store.getName());
		validateAddress(store.getAddress());
		validateLocation(store.getLatitude(), store.getLongitude());
	}

	public void validateExistingStore(final Store store, final Long userId) {
		validateStoreExists(store, ErrorCode.STORE_NOT_FOUND);
		validateNotDeleted(store);
		validateStoreOwnership(store, userId, ErrorCode.STORE_OWNER_ONLY);
	}

	private void validateStoreExists(final Store store, final ErrorCode errorCode) {
		if (store == null) {
			throw new BusinessException(errorCode);
		}
	}

	private void validateName(final String name) {
		if (name == null || name.isBlank()) {
			throw new BusinessException(ErrorCode.BAD_REQUEST);
		}
		if (name.length() > 100) {
			throw new BusinessException(ErrorCode.BAD_REQUEST);
		}
	}

	private void validateAddress(final String address) {
		if (address == null || address.isBlank()) {
			throw new BusinessException(ErrorCode.BAD_REQUEST);
		}
	}

	public void validateLocation(final BigDecimal latitude, final BigDecimal longitude) {
		if (latitude == null || longitude == null) {
			throw new BusinessException(ErrorCode.INVALID_LOCATION);
		}

		if (latitude.compareTo(BigDecimal.valueOf(-90.0)) < 0 ||
			latitude.compareTo(BigDecimal.valueOf(90.0)) > 0) {
			throw new BusinessException(ErrorCode.INVALID_LOCATION);
		}

		if (longitude.compareTo(BigDecimal.valueOf(-180.0)) < 0 ||
			longitude.compareTo(BigDecimal.valueOf(180.0)) > 0) {
			throw new BusinessException(ErrorCode.INVALID_LOCATION);
		}
	}

	public void validateStoreOwnership(final Store store, final Long userId, final ErrorCode errorCode) {
		if (!store.getOwnerId().equals(userId)) {
			throw new BusinessException(errorCode);
		}
	}

	private void validateNotDeleted(final Store store) {
		if (store.getDeletedAt() != null) {
			throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
		}
	}
}
