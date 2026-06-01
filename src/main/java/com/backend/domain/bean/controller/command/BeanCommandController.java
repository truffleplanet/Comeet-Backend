package com.backend.domain.bean.controller.command;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.common.auth.constants.RoleAuthority;
import com.backend.common.response.BaseResponse;
import com.backend.common.util.ResponseUtils;
import com.backend.domain.bean.dto.request.BeanCreateReqDto;
import com.backend.domain.bean.dto.request.BeanFlavorCreateReqDto;
import com.backend.domain.bean.dto.request.BeanUpdateReqDto;
import com.backend.domain.bean.dto.response.BeanFlavorResDto;
import com.backend.domain.bean.dto.response.BeanResDto;
import com.backend.domain.bean.service.command.BeanFlavorCommandService;
import com.backend.domain.bean.service.facade.BeanFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "Bean", description = "원두 관리 API")
@RestController
@RequestMapping("/beans")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BeanCommandController {

	private final BeanFacadeService beanFacadeService;
	private final BeanFlavorCommandService beanFlavorCommandService;

	@Operation(
		summary = "원두 생성",
		description = "새로운 원두를 등록합니다. OWNER 권한이 필요합니다."
	)
	@PreAuthorize(RoleAuthority.OWNER)
	@PostMapping
	public ResponseEntity<BaseResponse<BeanResDto>> createBean(
		@RequestBody @Valid BeanCreateReqDto reqDto
	) {
		return ResponseUtils.created(beanFacadeService.createBean(reqDto));
	}

	@Operation(
		summary = "원두 수정",
		description = "원두 정보를 수정합니다. OWNER 권한이 필요합니다."
	)
	@PreAuthorize(RoleAuthority.OWNER)
	@PatchMapping("/{beanId}")
	public ResponseEntity<BaseResponse<BeanResDto>> updateBean(
		@PathVariable Long beanId,
		@RequestBody @Valid BeanUpdateReqDto reqDto
	) {
		return ResponseUtils.ok(beanFacadeService.updateBean(beanId, reqDto));
	}

	@Operation(
		summary = "원두 삭제",
		description = "원두를 삭제(Soft Delete)합니다. OWNER 권한이 필요합니다."
	)
	@PreAuthorize(RoleAuthority.OWNER)
	@DeleteMapping("/{beanId}")
	public ResponseEntity<BaseResponse<Void>> deleteBean(
		@PathVariable Long beanId
	) {
		beanFacadeService.deleteBean(beanId);
		return ResponseUtils.noContent();
	}

	@Operation(
		summary = "원두-플레이버 매핑 추가",
		description = "원두에 플레이버를 추가 매핑합니다. OWNER 권한이 필요합니다."
	)
	@PreAuthorize(RoleAuthority.OWNER)
	@PostMapping("/{beanId}/flavors")
	public ResponseEntity<BaseResponse<BeanFlavorResDto>> addBeanFlavors(
		@PathVariable Long beanId,
		@RequestBody @Valid BeanFlavorCreateReqDto reqDto
	) {
		return ResponseUtils.created(beanFlavorCommandService.addBeanFlavors(beanId, reqDto));
	}

	@Operation(
		summary = "원두-플레이버 매핑 전체 교체",
		description = "원두의 플레이버 매핑을 전체 교체합니다. 기존 매핑을 삭제하고 새로운 매핑으로 대체합니다. OWNER 권한이 필요합니다."
	)
	@PreAuthorize(RoleAuthority.OWNER)
	@PutMapping("/{beanId}/flavors")
	public ResponseEntity<BaseResponse<BeanFlavorResDto>> updateBeanFlavors(
		@PathVariable Long beanId,
		@RequestBody @Valid BeanFlavorCreateReqDto reqDto
	) {
		return ResponseUtils.ok(beanFlavorCommandService.updateBeanFlavors(beanId, reqDto));
	}

	@Operation(
		summary = "원두-플레이버 매핑 전체 삭제",
		description = "원두의 플레이버 매핑을 전체 삭제합니다. OWNER 권한이 필요합니다."
	)
	@PreAuthorize(RoleAuthority.OWNER)
	@DeleteMapping("/{beanId}/flavors")
	public ResponseEntity<BaseResponse<Void>> deleteBeanFlavors(
		@PathVariable Long beanId
	) {
		beanFlavorCommandService.deleteBeanFlavorsByBeanId(beanId);
		return ResponseUtils.noContent();
	}
}
