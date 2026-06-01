package com.backend.domain.ai.controller.command;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.common.response.BaseResponse;
import com.backend.common.util.ResponseUtils;
import com.backend.domain.ai.dto.request.BatchImageGenerationReqDto;
import com.backend.domain.ai.dto.response.BatchImageGenerationResDto;
import com.backend.domain.ai.dto.response.BatchProgressResDto;
import com.backend.domain.ai.service.batch.BatchImageGenerationFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 배치 이미지 생성 컨트롤러
 * 관리자용 대량 이미지 생성 API
 */
@Slf4j
@Tag(name = "Batch AI", description = "배치 AI 이미지 생성 API")
@RestController
@RequestMapping("/batch/images")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchImageCommandController {

	private final BatchImageGenerationFacadeService facadeService;

	@PostMapping
	@Operation(summary = "배치 이미지 생성", description = "지정된 사용자들의 Passport 커버 이미지를 배치로 생성합니다")
	public ResponseEntity<BaseResponse<BatchImageGenerationResDto>> generateBatchImages(
		@Valid @RequestBody BatchImageGenerationReqDto request
	) {
		BatchImageGenerationResDto response = facadeService.generatePassportImagesForAllUsers(request.userIds());
		return ResponseUtils.ok(response);
	}

	@PostMapping("/month/{year}/{month}")
	@Operation(summary = "월별 전체 사용자 배치 이미지 생성",
		description = "특정 연도/월에 방문 기록이 있는 모든 사용자의 Passport 이미지를 배치로 생성합니다")
	public ResponseEntity<BaseResponse<BatchImageGenerationResDto>> generateMonthlyBatchImages(
		@PathVariable Integer year,
		@PathVariable Integer month
	) {
		BatchImageGenerationResDto response = facadeService.generateMonthlyBatchImages(year, month);
		return ResponseUtils.ok(response);
	}

	@GetMapping("/progress/{batchId}")
	@Operation(summary = "배치 진행 상황 조회", description = "배치 작업의 실시간 진행 상황을 조회합니다")
	public ResponseEntity<BaseResponse<BatchProgressResDto>> getProgress(
		@PathVariable String batchId
	) {
		return ResponseUtils.ok(facadeService.getProgress(batchId));
	}
}
