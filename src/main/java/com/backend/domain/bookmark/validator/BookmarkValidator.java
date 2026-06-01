package com.backend.domain.bookmark.validator;

import org.springframework.stereotype.Component;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.validator.Validator;
import com.backend.domain.bookmark.entity.BookmarkFolder;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BookmarkValidator implements Validator<BookmarkFolder> {

	@Override
	public void validate(final BookmarkFolder folder) {
		validateFolderExists(folder);
		validateName(folder.getName());
	}

	public void validateFolderExists(final BookmarkFolder folder) {
		if (folder == null) {
			throw new BusinessException(ErrorCode.BOOKMARK_FOLDER_NOT_FOUND);
		}
	}

	public void validateFolderOwnership(final BookmarkFolder folder, final Long userId) {
		validateFolderExists(folder);
		if (!folder.getUserId().equals(userId)) {
			throw new BusinessException(ErrorCode.BOOKMARK_FOLDER_ACCESS_DENIED);
		}
	}

	public void validateName(final String name) {
		if (name == null || name.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}

	public void validateItemNotExists(final boolean exists) {
		if (exists) {
			throw new BusinessException(ErrorCode.BOOKMARK_ITEM_ALREADY_EXISTS);
		}
	}

	public void validateItemExists(final boolean exists) {
		if (!exists) {
			throw new BusinessException(ErrorCode.BOOKMARK_ITEM_NOT_FOUND);
		}
	}
}
