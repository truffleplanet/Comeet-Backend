package com.backend.common.util;

import org.springframework.util.StringUtils;

import com.backend.common.enums.FileExtension;
import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtils {
	public static FileExtension getFileExtension(String fileName) {
		if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
			throw new BusinessException(ErrorCode.INVALID_FILE_NAME);
		}
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
		return FileExtension.fromExtension(extension);
	}

	public static FileExtension getFileExtensionFromMimeType(String mimeType) {
		if (!StringUtils.hasText(mimeType)) {
			throw new BusinessException(ErrorCode.INVALID_FILE_NAME);
		}
		return switch (mimeType) {
			case "image/png" -> FileExtension.PNG;
			case "image/jpeg", "image/jpg" -> FileExtension.JPEG;
			default -> throw new BusinessException(ErrorCode.INVALID_FILE_EXTENSION);
		};
	}
}
