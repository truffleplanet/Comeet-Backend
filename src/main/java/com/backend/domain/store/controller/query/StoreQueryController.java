package com.backend.domain.store.controller.query;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.common.annotation.CurrentUser;
import com.backend.common.auth.constants.RoleAuthority;
import com.backend.common.auth.principal.AuthenticatedUser;
import com.backend.common.response.BaseResponse;
import com.backend.common.response.PageResponse;
import com.backend.common.util.ResponseUtils;
import com.backend.domain.menu.dto.response.MenuResDto;
import com.backend.domain.review.dto.common.ReviewPageDto;
import com.backend.domain.store.dto.request.StoreSearchReqDto;
import com.backend.domain.store.dto.response.StoreDetailResDto;
import com.backend.domain.store.dto.response.StoreListResDto;
import com.backend.domain.store.service.facade.StoreFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "Store", description = "가맹점 관련 API")
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
class StoreQueryController {

	private final StoreFacadeService storeFacadeService;

	@Operation(
		summary = "가맹점 목록 조회",
		description = """
			사용자 위치 기반으로 주변 가맹점 목록을 조회합니다.
			- 거리순 정렬 (가까운 순)
			- 카테고리 및 키워드 필터링 지원
			- 리스트 뷰와 맵 뷰 모두에서 사용 가능 (마커 정보 포함)
			"""
	)
	@GetMapping
	public ResponseEntity<BaseResponse<StoreListResDto>> searchStores(
		@Parameter(
			description = "가맹점 검색 조건",
			required = true,
			schema = @Schema(implementation = StoreSearchReqDto.class)
		)
		@Valid @ModelAttribute final StoreSearchReqDto request
	) {
		final StoreListResDto response = storeFacadeService.searchStores(request);
		return ResponseUtils.ok(response);
	}

	@Operation(
		summary = "가맹점 상세 조회",
		description = "특정 가맹점의 상세 정보를 조회합니다."
	)
	@GetMapping("/{storeId}")
	public ResponseEntity<BaseResponse<StoreDetailResDto>> getStoreDetail(
		@Parameter(description = "가맹점 ID", example = "1")
		@PathVariable Long storeId
	) {
		StoreDetailResDto response = storeFacadeService.getStoreDetail(storeId);
		return ResponseUtils.ok(response);
	}

	@Operation(
		summary = "가맹점 리뷰 목록 조회",
		description = "특정 가맹점의 모든 리뷰 목록을 Flavor Badge와 함께 페이지네이션으로 조회합니다."
	)
	@GetMapping("/{storeId}/reviews")
	public ResponseEntity<PageResponse<ReviewPageDto>> getStoreReviews(
		@PathVariable Long storeId,
		@Parameter(description = "페이지 번호", example = "1")
		@RequestParam(defaultValue = "1") @Min(1) int page,
		@Parameter(description = "페이지 크기", example = "10")
		@RequestParam(defaultValue = "10") @Min(1) int size
	) {
		Page<ReviewPageDto> response = storeFacadeService.getReviewsByStore(storeId, page, size);
		return ResponseUtils.page(response);
	}

	@Operation(
		summary = "가맹점별 메뉴 목록 조회",
		description = "특정 가맹점의 모든 메뉴를 페이징하여 조회합니다."
	)
	@GetMapping("/{storeId}/menus")
	public ResponseEntity<PageResponse<MenuResDto>> getMenusByStore(
		@PathVariable Long storeId,
		@RequestParam(defaultValue = "1") @Min(1) int page,
		@RequestParam(defaultValue = "10") @Min(1) int size
	) {
		return ResponseUtils.page(storeFacadeService.getMenusByStore(storeId, page, size));
	}

	@Operation(
		summary = "내 가맹점 목록 조회",
		description = "로그인한 사용자가 소유한 가맹점 목록을 조회합니다. OWNER(가맹점주) 권한이 필요합니다."
	)
	@GetMapping("/my")
	@PreAuthorize(RoleAuthority.OWNER)
	public ResponseEntity<BaseResponse<List<StoreDetailResDto>>> getMyStores(
		@CurrentUser AuthenticatedUser user
	) {
		List<StoreDetailResDto> response = storeFacadeService.findMyStores(user.getUser().getId());
		return ResponseUtils.ok(response);
	}

}
