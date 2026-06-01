package com.backend.domain.image.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.common.response.BaseResponse;
import com.backend.common.util.ResponseUtils;
import com.backend.domain.image.dto.ImageResDto;
import com.backend.domain.image.service.facade.ImageFacadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Tag(name = "Image", description = "이미지 관리 API")
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageController {

	private final ImageFacadeService imageFacadeService;

	@Operation(summary = " 이미지 업로드 API", description = "이미지를 업로드합니다.")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<BaseResponse<ImageResDto>> uploadImage(
		@RequestPart("image") @NotNull MultipartFile image
	) {
		ImageResDto imageResDto = imageFacadeService.uploadImage(image);
		return ResponseUtils.ok(imageResDto);
	}
}
