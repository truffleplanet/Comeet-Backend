package com.backend.domain.menu.service.facade;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.util.PageUtils;
import com.backend.domain.bean.entity.Bean;
import com.backend.domain.bean.service.query.BeanQueryService;
import com.backend.domain.menu.converter.MenuConverter;
import com.backend.domain.menu.dto.request.MenuBeanMappingReqDto;
import com.backend.domain.menu.dto.request.MenuCreateReqDto;
import com.backend.domain.menu.dto.request.MenuUpdateReqDto;
import com.backend.domain.menu.dto.response.MenuBeanMappingResDto;
import com.backend.domain.menu.dto.response.MenuDetailResDto;
import com.backend.domain.menu.dto.response.MenuResDto;
import com.backend.domain.menu.entity.Menu;
import com.backend.domain.menu.factory.MenuFactory;
import com.backend.domain.menu.service.command.MenuCommandService;
import com.backend.domain.menu.service.query.MenuQueryService;
import com.backend.domain.menu.validator.MenuValidator;
import com.backend.domain.store.entity.Store;
import com.backend.domain.store.service.query.StoreQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuFacadeService {

	private final MenuCommandService menuCommandService;
	private final MenuQueryService menuQueryService;
	private final MenuFactory menuFactory;

	private final StoreQueryService storeQueryService;
	private final BeanQueryService beanQueryService;

	public MenuResDto createMenu(Long storeId, Long userId, MenuCreateReqDto reqDto) {
		validateStoreOwnership(storeId, userId);

		Menu menu = menuFactory.create(storeId, reqDto);
		menuCommandService.insert(menu);

		return MenuConverter.toMenuResDto(menu);
	}

	public Page<MenuResDto> getMenusByStore(Long storeId, int page, int size) {
		return PageUtils.buildPageResponse(
			page, size,
			pageable -> menuQueryService.findByStoreId(storeId, pageable),
			() -> menuQueryService.countByStoreId(storeId),
			MenuConverter::toMenuResDto
		);
	}

	public MenuDetailResDto getMenuDetail(Long menuId) {
		Menu menu = menuQueryService.findById(menuId);
		List<Bean> beans = beanQueryService.findByMenuId(menuId);

		return MenuConverter.toMenuDetailResDto(menu, beans);
	}

	public MenuResDto updateMenu(Long menuId, Long userId, MenuUpdateReqDto reqDto) {
		Menu existingMenu = menuQueryService.findById(menuId);
		MenuValidator.validateNotDeleted(existingMenu);

		validateStoreOwnership(existingMenu.getStoreId(), userId);

		Menu updatedMenu = menuFactory.createForUpdate(existingMenu, reqDto);
		menuCommandService.update(updatedMenu);

		return MenuConverter.toMenuResDto(updatedMenu);
	}

	public void deleteMenu(Long menuId, Long userId) {
		Menu menu = menuQueryService.findById(menuId);
		MenuValidator.validateNotDeleted(menu);

		validateStoreOwnership(menu.getStoreId(), userId);

		menuCommandService.softDelete(menuId);
	}

	@Transactional
	public MenuBeanMappingResDto addBeanToMenu(Long menuId, Long userId, MenuBeanMappingReqDto reqDto) {
		Menu menu = menuQueryService.findById(menuId);
		validateStoreOwnership(menu.getStoreId(), userId);

		int existingMappingCount = menuQueryService.countMenuBeanMapping(menuId, reqDto.beanId());
		if (existingMappingCount > 0) {
			throw new BusinessException(ErrorCode.MENU_BEAN_ALREADY_MAPPED);
		}

		menuCommandService.insertMenuBeanMapping(menuId, reqDto.beanId(), reqDto.isBlended());

		//* 예외 발생 없이 코드 진행 되었을 경우 연결되었다고 판단
		return MenuConverter.toMenuBeanMappingResDto(menuId, reqDto, true);
	}

	public void removeBeanFromMenu(Long menuId, Long beanId, Long userId) {
		Menu menu = menuQueryService.findById(menuId);
		validateStoreOwnership(menu.getStoreId(), userId);

		menuCommandService.deleteMenuBeanMapping(menuId, beanId);
	}

	private void validateStoreOwnership(Long storeId, Long userId) {
		Store store = storeQueryService.findById(storeId);
		MenuValidator.validateStoreOwnership(store, userId);
	}
}
