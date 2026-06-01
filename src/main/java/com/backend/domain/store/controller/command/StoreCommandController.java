package com.backend.domain.store.controller.command;

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
import com.backend.domain.menu.dto.request.MenuCreateReqDto;
import com.backend.domain.menu.dto.response.MenuResDto;
import com.backend.domain.store.dto.request.StoreCreateReqDto;
import com.backend.domain.store.dto.request.StoreUpdateReqDto;
import com.backend.domain.store.dto.response.StoreDetailResDto;
import com.backend.domain.store.service.facade.StoreFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "Store", description = "가맹점 관리 API")
@RestController
@RequestMapping("/stores")
@PreAuthorize(RoleAuthority.OWNER)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class StoreCommandController {

	private final StoreFacadeService storeFacadeService;

	@Operation(
		summary = "메뉴 추가",
		description = "특정 가맹점에 새로운 메뉴를 추가합니다."
	)
	@PostMapping("/{storeId}/menus")
	public ResponseEntity<BaseResponse<MenuResDto>> createMenu(
		@PathVariable Long storeId,
		@CurrentUser AuthenticatedUser user,
		@RequestBody @Valid MenuCreateReqDto reqDto
	) {
		return ResponseUtils.created(storeFacadeService.createMenuForStore(storeId, user.getUser().getId(), reqDto));
	}

	@Operation(
		summary = "가맹점 등록",
		description = "새로운 가맹점을 등록합니다. OWNER(가맹점주) 권한이 필요합니다."
	)
	@PostMapping
	public ResponseEntity<BaseResponse<StoreDetailResDto>> createStore(
		@CurrentUser AuthenticatedUser user,
		@RequestBody @Valid StoreCreateReqDto reqDto
	) {
		StoreDetailResDto response = storeFacadeService.createStore(reqDto, user.getUser().getId());
		return ResponseUtils.created(response);
	}

	@Operation(
		summary = "가맹점 수정",
		description = "가맹점 정보를 수정합니다. 본인 소유의 가맹점만 수정할 수 있습니다."
	)
	@PutMapping("/{storeId}")
	public ResponseEntity<BaseResponse<StoreDetailResDto>> updateStore(
		@PathVariable Long storeId,
		@CurrentUser AuthenticatedUser user,
		@RequestBody @Valid StoreUpdateReqDto reqDto
	) {
		StoreDetailResDto response = storeFacadeService.updateStore(storeId, reqDto, user.getUser().getId());
		return ResponseUtils.ok(response);
	}

	@Operation(
		summary = "가맹점 삭제",
		description = "가맹점을 삭제합니다 (Soft Delete). 본인 소유의 가맹점만 삭제할 수 있습니다."
	)
	@DeleteMapping("/{storeId}")
	public ResponseEntity<BaseResponse<Void>> deleteStore(
		@PathVariable Long storeId,
		@CurrentUser AuthenticatedUser user
	) {
		storeFacadeService.deleteStore(storeId, user.getUser().getId());
		return ResponseUtils.noContent();
	}
}
