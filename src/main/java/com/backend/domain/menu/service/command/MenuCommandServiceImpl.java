package com.backend.domain.menu.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.domain.menu.entity.Menu;
import com.backend.domain.menu.mapper.command.MenuCommandMapper;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuCommandServiceImpl implements MenuCommandService {

	private final MenuCommandMapper menuCommandMapper;

	@Override
	public void insert(Menu menu) {
		log.info("[Menu] 메뉴 저장 - storeId: {}, name: {}", menu.getStoreId(), menu.getName());
		int affectedRows = menuCommandMapper.insert(menu);
		if (affectedRows == 0) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}
	}

	@Override
	public void update(Menu menu) {
		log.info("[Menu] 메뉴 업데이트 - id: {}, name: {}", menu.getId(), menu.getName());
		int affectedRows = menuCommandMapper.update(menu);
		if (affectedRows == 0) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}
	}

	@Override
	public void softDelete(Long menuId) {
		log.info("[Menu] 메뉴 소프트 삭제 - id: {}", menuId);
		int affectedRows = menuCommandMapper.softDelete(menuId);
		if (affectedRows == 0) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}
	}

	@Override
	public void insertMenuBeanMapping(Long menuId, Long beanId, boolean isBlended) {
		log.info("[Menu] 메뉴-원두 매핑 생성 - menuId: {}, beanId: {}", menuId, beanId);
		int affectedRows = menuCommandMapper.insertMenuBeanMapping(menuId, beanId, isBlended);
		if (affectedRows == 0) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}
	}

	@Override
	public void deleteMenuBeanMapping(Long menuId, Long beanId) {
		log.info("[Menu] 메뉴-원두 매핑 삭제 - menuId: {}, beanId: {}", menuId, beanId);
		int affectedRows = menuCommandMapper.deleteMenuBeanMapping(menuId, beanId);
		if (affectedRows == 0) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR);
		}
	}
}
