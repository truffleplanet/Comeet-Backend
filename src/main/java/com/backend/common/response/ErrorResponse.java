package com.backend.common.response;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.backend.common.error.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;

@Builder
public record ErrorResponse(
	HttpStatus status,
	String code,
	String message,
	String method,
	String requestURI,
	List<FieldErrorDetail> errors
) {

	public static ErrorResponse of(ErrorCode errorCode, HttpServletRequest request) {
		return ErrorResponse.builder()
			.status(errorCode.getHttpStatus())
			.code(errorCode.getCode())
			.message(errorCode.getMessage())
			.method(request.getMethod())
			.requestURI(request.getRequestURI())
			.errors(new ArrayList<>())
			.build();
	}

	public static ErrorResponse of(HttpServletRequest request, ErrorCode errorCode, final String errorMessage) {
		return ErrorResponse.builder()
			.status(errorCode.getHttpStatus())
			.code(errorCode.getCode())
			.message(errorMessage)
			.method(request.getMethod())
			.requestURI(request.getRequestURI())
			.errors(new ArrayList<>())
			.build();
	}

	public void addValidationErrors(BindingResult bindingResult) {
		bindingResult.getFieldErrors().forEach(this::addValidationError);
	}

	private void addValidationError(FieldError fieldError) {
		this.errors.add(
			FieldErrorDetail.builder()
				.field(fieldError.getField())
				.message(fieldError.getDefaultMessage())
				.build()
		);
	}

	@Builder
	public record FieldErrorDetail(String field, String message) {
	}
}
