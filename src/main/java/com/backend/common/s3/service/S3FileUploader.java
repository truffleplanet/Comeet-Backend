package com.backend.common.s3.service;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.backend.common.component.FileUploader;
import com.backend.common.config.property.S3Property;
import com.backend.common.enums.FileExtension;
import com.backend.common.enums.FileType;
import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.util.FileUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3FileUploader implements FileUploader {

	private final S3Property s3Property;

	private final S3Client s3Client;

	@Override
	public String uploadFile(final MultipartFile file, FileType type) {
		String originalFilename = file.getOriginalFilename();
		FileExtension extension = FileUtils.getFileExtension(originalFilename);

		validateFileExtensions(type, extension);

		String key = FileUploader.generateKey(type.getDirectory(), extension);

		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(s3Property.bucket())
			.key(key)
			.contentType(file.getContentType())
			.contentLength(file.getSize())
			.build();

		putObject(file, putObjectRequest);

		return s3Client.utilities()
			.getUrl(GetUrlRequest.builder()
				.bucket(s3Property.bucket())
				.key(key)
				.build()
			).toExternalForm();
	}

	public String uploadFile(final byte[] fileBytes, final String contentType, final FileType type) {
		FileExtension extension = FileUtils.getFileExtensionFromMimeType(contentType);

		validateFileExtensions(type, extension);

		String key = FileUploader.generateKey(type.getDirectory(), extension);
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
			.bucket(s3Property.bucket())
			.key(key)
			.contentType(contentType)
			.contentLength((long)fileBytes.length)
			.build();

		s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));

		return s3Client.utilities()
			.getUrl(GetUrlRequest.builder()
				.bucket(s3Property.bucket())
				.key(key)
				.build()
			).toExternalForm();
	}

	private void validateFileExtensions(FileType type, FileExtension extension) {
		if (!type.isSupportedExtension(extension)) {
			throw new BusinessException(ErrorCode.INVALID_FILE_EXTENSION);
		}
	}

	private void putObject(MultipartFile file, PutObjectRequest putObjectRequest) {
		try {
			s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
		} catch (IOException e) {
			log.error("[S3] 파일 업로드 에러 bucket: {}, key: {}", putObjectRequest.bucket(), putObjectRequest.key());
			throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
		}
	}
}
