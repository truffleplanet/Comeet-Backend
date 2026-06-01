package com.backend.domain.menu.controller.command;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.common.annotation.CurrentUser;
import com.backend.common.auth.constants.RoleAuthority;
import com.backend.common.auth.principal.AuthenticatedUser;
import com.backend.common.response.BaseResponse;
import com.backend.common.util.ResponseUtils;
import com.backend.domain.menu.dto.request.MenuBeanMappingReqDto;
import com.backend.domain.menu.dto.request.MenuUpdateReqDto;
import com.backend.domain.menu.dto.response.MenuBeanMappingResDto;
import com.backend.domain.menu.dto.response.MenuResDto;
import com.backend.domain.menu.service.facade.MenuFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "Menu", description = "메뉴 관리 API")
@RestController
@RequestMapping("/menus")
@PreAuthorize(RoleAuthority.OWNER)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuCommandController {

	private final MenuFacadeService menuFacadeService;

	@Operation(
		summary = "메뉴 수정",
		description = "메뉴 정보를 수정합니다. 해당 가맹점의 소유자만 수정할 수 있습니다."
	)
	@PutMapping("/{menuId}")
	public ResponseEntity<BaseResponse<MenuResDto>> updateMenu(
		@PathVariable Long menuId,
		@CurrentUser AuthenticatedUser user,
		@RequestBody @Valid MenuUpdateReqDto reqDto
	) {
		return ResponseUtils.ok(menuFacadeService.updateMenu(menuId, user.getUser().getId(), reqDto));
	}

	@Operation(
		summary = "메뉴 삭제",
		description = "메뉴를 소프트 삭제합니다. 해당 가맹점의 소유자만 삭제할 수 있습니다."
	)
	@DeleteMapping("/{menuId}")
	public ResponseEntity<BaseResponse<Void>> deleteMenu(
		@PathVariable Long menuId,
		@CurrentUser AuthenticatedUser user
	) {
		menuFacadeService.deleteMenu(menuId, user.getUser().getId());
		return ResponseUtils.noContent();
	}

	@Operation(
		summary = "메뉴-원두 연결",
		description = "메뉴에 원두를 연결합니다. 해당 가맹점의 소유자만 연결할 수 있습니다."
	)
	@PostMapping("/{menuId}/beans")
	public ResponseEntity<BaseResponse<MenuBeanMappingResDto>> addBeanToMenu(
		@PathVariable Long menuId,
		@CurrentUser AuthenticatedUser user,
		@RequestBody @Valid MenuBeanMappingReqDto reqDto
	) {
		return ResponseUtils.created(menuFacadeService.addBeanToMenu(menuId, user.getUser().getId(), reqDto));
	}

	@Operation(
		summary = "메뉴-원두 연결 해제",
		description = "메뉴에서 원두 연결을 해제합니다. 해당 가맹점의 소유자만 해제할 수 있습니다."
	)
	@DeleteMapping("/{menuId}/beans/{beanId}")
	public ResponseEntity<BaseResponse<Void>> removeBeanFromMenu(
		@PathVariable Long menuId,
		@PathVariable Long beanId,
		@CurrentUser AuthenticatedUser user
	) {
		menuFacadeService.removeBeanFromMenu(menuId, beanId, user.getUser().getId());
		return ResponseUtils.noContent();
	}
}

