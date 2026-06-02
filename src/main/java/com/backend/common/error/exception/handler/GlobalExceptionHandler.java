package com.backend.common.error.exception.handler;

import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;
import com.backend.common.response.BaseResponse;
import com.backend.common.response.ErrorResponse;
import com.backend.common.util.LoggingUtil;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<BaseResponse<ErrorResponse>> handleBusinessException(
		BusinessException ex,
		HttpServletRequest request
	) {
		LoggingUtil.logBusinessException(ex, request);
		ErrorResponse response = ErrorResponse.of(ex.getErrorCode(), request);
		return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(BaseResponse.fail(response));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<BaseResponse<ErrorResponse>> handleMethodArgumentNotValid(
		MethodArgumentNotValidException ex,
		HttpServletRequest request
	) {
		LoggingUtil.logValidationException(ex, request);
		ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT, request);
		response.addValidationErrors(ex.getBindingResult());
		return ResponseEntity.status(ErrorCode.INVALID_INPUT.getHttpStatus()).body(BaseResponse.fail(response));
	}

	@ExceptionHandler({
		MethodArgumentTypeMismatchException.class,
		HttpMessageNotReadableException.class,
		IllegalArgumentException.class
	})
	public ResponseEntity<BaseResponse<ErrorResponse>> handleInvalidInput(Exception ex, HttpServletRequest request) {
		log.warn(
			"Invalid input - request: [{} {}], message: {}",
			request.getMethod(),
			request.getRequestURI(),
			ex.getMessage()
		);
		ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT, request);
		return ResponseEntity.status(ErrorCode.INVALID_INPUT.getHttpStatus()).body(BaseResponse.fail(response));
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<BaseResponse<ErrorResponse>> handleDataAccessException(
		DataAccessException ex,
		HttpServletRequest request
	) {
		LoggingUtil.logSystemException("DataAccessException", ex, request);
		ErrorResponse response = ErrorResponse.of(ErrorCode.DATABASE_ERROR, request);
		return ResponseEntity.status(ErrorCode.DATABASE_ERROR.getHttpStatus()).body(BaseResponse.fail(response));
	}

	@ExceptionHandler(DuplicateKeyException.class)
	public ResponseEntity<BaseResponse<ErrorResponse>> handleDuplicateKeyException(
		DuplicateKeyException ex,
		HttpServletRequest request
	) {
		LoggingUtil.logSystemException("DuplicateKeyException", ex, request);
		ErrorResponse response = ErrorResponse.of(ErrorCode.DUPLICATED_KEY, request);
		return ResponseEntity.status(ErrorCode.DUPLICATED_KEY.getHttpStatus()).body(BaseResponse.fail(response));
	}

	@ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
	public ResponseEntity<BaseResponse<ErrorResponse>> handleEndpointNotFound(Exception ex, HttpServletRequest request) {
		LoggingUtil.logNotFoundException(ex, request);
		ErrorResponse response = ErrorResponse.of(ErrorCode.NOT_FOUND, request);
		return ResponseEntity.status(ErrorCode.NOT_FOUND.getHttpStatus()).body(BaseResponse.fail(response));
	}

	@ExceptionHandler(SQLException.class)
	public ResponseEntity<BaseResponse<ErrorResponse>> handleSQLException(SQLException ex, HttpServletRequest request) {
		LoggingUtil.logSystemException("SQLException", ex, request);
		ErrorResponse response = ErrorResponse.of(ErrorCode.DATABASE_ERROR, request);
		return ResponseEntity.status(ErrorCode.DATABASE_ERROR.getHttpStatus()).body(BaseResponse.fail(response));
	}

	@ExceptionHandler(JwtException.class)
	public ResponseEntity<BaseResponse<ErrorResponse>> handleJwtException(JwtException ex, HttpServletRequest request) {
		LoggingUtil.logSystemException("JwtException", ex, request);
		ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_TOKEN, request);
		return ResponseEntity.status(ErrorCode.INVALID_TOKEN.getHttpStatus()).body(BaseResponse.fail(response));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<BaseResponse<ErrorResponse>> handleException(Exception ex, HttpServletRequest request) {
		LoggingUtil.logSystemException("Unhandled exception", ex, request);
		ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, request);
		return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus()).body(BaseResponse.fail(response));
	}
}
