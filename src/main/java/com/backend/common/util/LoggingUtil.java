package com.backend.common.util;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.backend.common.error.exception.BusinessException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class LoggingUtil {

	public void logBusinessException(BusinessException ex, HttpServletRequest request) {
		HttpStatus status = ex.getErrorCode().getHttpStatus();

		if (status.is5xxServerError()) {
			log.error(
				"BusinessException - code: {}, status: {}, request: [{} {}], message: {}",
				ex.getErrorCode().getCode(),
				status.value(),
				request.getMethod(),
				request.getRequestURI(),
				ex.getMessage(),
				ex
			);
			return;
		}

		log.warn(
			"BusinessException - code: {}, status: {}, request: [{} {}], message: {}",
			ex.getErrorCode().getCode(),
			status.value(),
			request.getMethod(),
			request.getRequestURI(),
			ex.getMessage()
		);
	}

	public void logSystemException(String prefix, Exception ex, HttpServletRequest request) {
		log.error(
			"{} - request: [{} {}], message: {}",
			prefix,
			request.getMethod(),
			request.getRequestURI(),
			ex.getMessage(),
			ex
		);
	}

	public void logValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
		String errorFields = ex.getBindingResult().getFieldErrors().stream()
			.map(LoggingUtil::formatFieldError)
			.collect(Collectors.joining(" "));

		log.warn(
			"Validation failed - request: [{} {}], errors: {}",
			request.getMethod(),
			request.getRequestURI(),
			errorFields
		);
	}

	public void logNotFoundException(Exception ex, HttpServletRequest request) {
		log.warn(
			"Endpoint not found - request: [{} {}], message: {}",
			request.getMethod(),
			request.getRequestURI(),
			ex.getMessage()
		);
	}

	private String formatFieldError(FieldError error) {
		return String.format("[field: %s, message: %s]", error.getField(), error.getDefaultMessage());
	}
}
