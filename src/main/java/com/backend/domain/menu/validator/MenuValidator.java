package com.backend.domain.menu.validator;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.validator.Validator;
import com.backend.domain.menu.entity.Menu;
import com.backend.domain.store.entity.Store;

@Component
public class MenuValidator implements Validator<Menu> {

	@Override
	public void validate(final Menu menu) {
		validateNotNull(menu);
		validateName(menu.getName());
		validatePrice(menu.getPrice());
		validateCategory(menu.getCategory());
	}

	private void validateNotNull(final Menu menu) {
		if (menu == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}

	private void validateName(final String name) {
		if (name == null || name.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}

	private void validatePrice(final Integer price) {
		if (price == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		if (price <= 0) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}

	private void validateCategory(final Object category) {
		if (category == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}

	public static void validateStoreOwnership(final Store store, final Long userId) {
		if (!Objects.equals(store.getOwnerId(), userId)) {
			throw new BusinessException(ErrorCode.MENU_ACCESS_DENIED);
		}
	}

	public static void validateNotDeleted(final Menu menu) {
		if (menu.getDeletedAt() != null) {
			throw new BusinessException(ErrorCode.MENU_ALREADY_DELETED);
		}
	}
}
