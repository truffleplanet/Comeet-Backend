package com.backend.domain.beanscore.controller.command;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.common.response.BaseResponse;
import com.backend.common.util.ResponseUtils;
import com.backend.domain.beanscore.batch.BeanEmbeddingBatchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "BeanScore Admin", description = "원두 점수 관리자 API")
@RestController
@RequestMapping("/admin/bean-scores")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BeanEmbeddingController {

	private final BeanEmbeddingBatchService beanEmbeddingBatchService;

	@Operation(
		summary = "전체 원두 임베딩 생성",
		description = "모든 원두의 flavor_tags를 임베딩하여 Redis Vector에 저장합니다. 관리자 전용 API입니다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "임베딩 생성 성공"),
		@ApiResponse(responseCode = "403", description = "관리자 권한 필요")
	})
	@PostMapping("/embed-all")
	public ResponseEntity<BaseResponse<EmbedAllResponse>> embedAllBeans() {
		int processedCount = beanEmbeddingBatchService.embedAllBeans();
		return ResponseUtils.ok(new EmbedAllResponse(processedCount, "전체 원두 임베딩 생성 완료"));
	}

	@Operation(
		summary = "누락된 원두 임베딩 생성",
		description = "임베딩이 없는 원두만 처리합니다. flavor가 없는 원두는 skip됩니다. 관리자 전용 API입니다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "임베딩 생성 성공"),
		@ApiResponse(responseCode = "403", description = "관리자 권한 필요")
	})
	@PostMapping("/embed-missing")
	public ResponseEntity<BaseResponse<EmbedAllResponse>> embedMissingBeans() {
		int processedCount = beanEmbeddingBatchService.embedMissingBeans();
		return ResponseUtils.ok(new EmbedAllResponse(processedCount, "누락된 원두 임베딩 생성 완료"));
	}

	@Operation(
		summary = "전체 임베딩 삭제 후 재생성",
		description = "기존 임베딩을 모두 삭제하고, flavor가 있는 원두만 새로 임베딩합니다. 관리자 전용 API입니다."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "임베딩 재생성 성공"),
		@ApiResponse(responseCode = "403", description = "관리자 권한 필요")
	})
	@PostMapping("/drop-and-embed")
	public ResponseEntity<BaseResponse<DropAndEmbedResponse>> dropAndEmbedAll() {
		BeanEmbeddingBatchService.EmbedResult result = beanEmbeddingBatchService.dropAndEmbedAll();
		return ResponseUtils.ok(new DropAndEmbedResponse(
			result.deletedCount(),
			result.embeddedCount(),
			"기존 임베딩 삭제 후 재생성 완료 (flavor가 있는 원두만)"
		));
	}

	public record EmbedAllResponse(
		int processedCount,
		String message
	) {
	}

	public record DropAndEmbedResponse(
		int deletedCount,
		int embeddedCount,
		String message
	) {
	}
}
