package com.backend.domain.menu.service.query;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.menu.entity.Menu;
import com.backend.domain.menu.mapper.query.MenuQueryMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuQueryServiceImpl implements MenuQueryService {

	private final MenuQueryMapper menuQueryMapper;

	@Override
	public Menu findById(Long menuId) {
		log.debug("[Menu] 메뉴 조회 - id: {}", menuId);
		return menuQueryMapper.findById(menuId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
	}

	@Override
	public List<Menu> findByStoreId(Long storeId, Pageable pageable) {
		log.debug("[Menu] 가맹점별 메뉴 목록 조회 - storeId: {}", storeId);
		return menuQueryMapper.findByStoreId(storeId, pageable);
	}

	@Override
	public int countByStoreId(Long storeId) {
		return menuQueryMapper.countByStoreId(storeId);
	}

	@Override
	public int countMenuBeanMapping(final Long menuId, final Long beanId) {
		return menuQueryMapper.countMenuBeanMapping(menuId, beanId);
	}
}
