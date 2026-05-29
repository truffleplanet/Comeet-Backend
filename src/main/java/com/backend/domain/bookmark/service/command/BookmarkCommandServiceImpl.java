package com.backend.domain.bookmark.service.command;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.bookmark.converter.BookmarkConverter;
import com.backend.domain.bookmark.dto.response.BookmarkItemResDto;
import com.backend.domain.bookmark.entity.BookmarkFolder;
import com.backend.domain.bookmark.entity.BookmarkItem;
import com.backend.domain.bookmark.mapper.command.BookmarkCommandMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BookmarkCommandServiceImpl implements BookmarkCommandService {

	private final BookmarkCommandMapper commandMapper;

	@Override
	public BookmarkFolder createFolder(final BookmarkFolder folder) {
		commandMapper.saveFolder(folder);
		log.info("[Bookmark] 폴더 생성 완료 - ID: {}, name: {}", folder.getId(), folder.getName());
		return folder;
	}

	@Override
	public BookmarkFolder updateFolder(final BookmarkFolder folder) {
		int affectedRows = commandMapper.updateFolder(folder);

		if (affectedRows == 0) {
			log.error("[Bookmark] 폴더 수정 실패 - ID: {}", folder.getId());
			throw new BusinessException(ErrorCode.BOOKMARK_FOLDER_NOT_FOUND);
		}

		log.info("[Bookmark] 폴더 수정 완료 - ID: {}", folder.getId());
		return folder;
	}

	@Override
	public void deleteFolder(final Long folderId) {
		int affectedRows = commandMapper.deleteFolder(folderId);

		if (affectedRows == 0) {
			log.error("[Bookmark] 폴더 삭제 실패 - ID: {}", folderId);
			throw new BusinessException(ErrorCode.BOOKMARK_FOLDER_NOT_FOUND);
		}

		log.info("[Bookmark] 폴더 삭제 완료 - ID: {}", folderId);
	}

	@Override
	public BookmarkItemResDto addStoreToFolder(final Long folderId, final Long storeId) {
		BookmarkItem item = BookmarkConverter.toItemEntity(folderId, storeId);
		commandMapper.saveItem(item);

		log.info("[Bookmark] 카페 추가 완료 - folderId: {}, storeId: {}", folderId, storeId);
		return BookmarkConverter.toItemResDto(folderId, storeId, LocalDateTime.now());
	}

	@Override
	public void removeStoreFromFolder(final Long folderId, final Long storeId) {
		int affectedRows = commandMapper.deleteItem(folderId, storeId);

		if (affectedRows == 0) {
			log.error("[Bookmark] 카페 삭제 실패 - folderId: {}, storeId: {}", folderId, storeId);
			throw new BusinessException(ErrorCode.BOOKMARK_ITEM_NOT_FOUND);
		}

		log.info("[Bookmark] 카페 삭제 완료 - folderId: {}, storeId: {}", folderId, storeId);
	}
}
