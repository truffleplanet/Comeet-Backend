package com.backend.domain.bookmark.service.query;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.bookmark.converter.BookmarkConverter;
import com.backend.domain.bookmark.dto.response.BookmarkStatusResDto;
import com.backend.domain.bookmark.dto.response.BookmarkedStoreResDto;
import com.backend.domain.bookmark.dto.response.FolderResDto;
import com.backend.domain.bookmark.entity.BookmarkFolder;
import com.backend.domain.bookmark.mapper.query.BookmarkQueryMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BookmarkQueryServiceImpl implements BookmarkQueryService {

	private final BookmarkQueryMapper queryMapper;

	@Override
	public List<FolderResDto> findFoldersByUserId(final Long userId) {
		List<FolderResDto> folders = queryMapper.findFoldersByUserId(userId);
		log.debug("[Bookmark] 사용자 폴더 목록 조회 - userId: {}, count: {}", userId, folders.size());
		return folders;
	}

	@Override
	public BookmarkFolder findFolderById(final Long folderId) {
		log.debug("[Bookmark] 폴더 조회 - folderId: {}", folderId);
		return queryMapper.findFolderById(folderId)
			.orElseThrow(() -> new BusinessException(ErrorCode.BOOKMARK_FOLDER_NOT_FOUND));
	}

	@Override
	public List<BookmarkedStoreResDto> findStoresByFolderId(final Long folderId) {
		List<BookmarkedStoreResDto> stores = queryMapper.findStoresByFolderId(folderId);
		log.debug("[Bookmark] 폴더 내 카페 목록 조회 - folderId: {}, count: {}", folderId, stores.size());
		return stores;
	}

	@Override
	public BookmarkStatusResDto getBookmarkStatus(final Long userId, final Long storeId) {
		List<BookmarkStatusResDto.BookmarkedFolderInfo> folders =
			queryMapper.findFoldersByUserIdAndStoreId(userId, storeId);
		log.debug("[Bookmark] 북마크 상태 조회 - userId: {}, storeId: {}, bookmarked: {}",
			userId, storeId, !folders.isEmpty());
		return BookmarkConverter.toBookmarkStatusResDto(folders);
	}

	@Override
	public boolean existsItem(final Long folderId, final Long storeId) {
		return queryMapper.existsItemByFolderIdAndStoreId(folderId, storeId);
	}
}
