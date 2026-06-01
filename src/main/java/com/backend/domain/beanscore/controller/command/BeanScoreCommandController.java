package com.backend.domain.beanscore.controller.command;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.common.response.BaseResponse;
import com.backend.common.util.ResponseUtils;
import com.backend.domain.beanscore.dto.request.BeanScoreUpdateReqDto;
import com.backend.domain.beanscore.dto.response.BeanScoreResDto;
import com.backend.domain.beanscore.service.facade.BeanScoreFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "BeanScore", description = "원두 점수 관련 API")
@RestController
@RequestMapping("/bean-scores")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BeanScoreCommandController {

	private final BeanScoreFacadeService beanScoreFacadeService;

	@Operation(
		summary = "원두 점수 생성",
		description = "특정 원두의 점수 정보를 생성합니다. 이미 점수가 존재하면 에러가 발생합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "원두 점수 생성 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "409", description = "이미 원두 점수가 존재함")
	})
	@PostMapping("/{beanId}")
	public ResponseEntity<BaseResponse<BeanScoreResDto>> createBeanScore(
		@Parameter(description = "원두 ID", required = true)
		@PathVariable Long beanId,
		@RequestBody @Valid BeanScoreUpdateReqDto reqDto
	) {
		BeanScoreResDto response = beanScoreFacadeService.createBeanScore(beanId, reqDto);
		return ResponseUtils.created(response);
	}

	@Operation(
		summary = "원두 점수 업데이트",
		description = "특정 원두의 점수 정보를 업데이트합니다. 점수가 없으면 자동으로 생성됩니다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "원두 점수 업데이트 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	@PutMapping("/{beanId}")
	public ResponseEntity<BaseResponse<BeanScoreResDto>> updateBeanScore(
		@Parameter(description = "원두 ID", required = true)
		@PathVariable Long beanId,
		@RequestBody @Valid BeanScoreUpdateReqDto reqDto
	) {
		BeanScoreResDto response = beanScoreFacadeService.updateBeanScore(beanId, reqDto);
		return ResponseUtils.ok(response);
	}

	@Operation(
		summary = "원두 점수 삭제",
		description = "특정 원두의 점수 정보를 삭제합니다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "원두 점수 삭제 성공"),
		@ApiResponse(responseCode = "404", description = "원두 점수를 찾을 수 없음")
	})
	@DeleteMapping("/{beanId}")
	public ResponseEntity<Void> deleteBeanScore(
		@Parameter(description = "원두 ID", required = true)
		@PathVariable Long beanId
	) {
		beanScoreFacadeService.deleteBeanScore(beanId);
		return ResponseEntity.noContent().build();
	}
}
