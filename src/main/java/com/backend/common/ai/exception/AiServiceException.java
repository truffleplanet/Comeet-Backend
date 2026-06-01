package com.backend.common.ai.exception;

public class AiServiceException extends RuntimeException {

	public AiServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
