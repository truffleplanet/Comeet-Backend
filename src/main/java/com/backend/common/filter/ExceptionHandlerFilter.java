package com.backend.common.filter;

import java.io.IOException;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ExceptionHandlerFilter extends OncePerRequestFilter {

	private final HandlerExceptionResolver exceptionResolver;

	public ExceptionHandlerFilter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
		this.exceptionResolver = resolver;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) {
		try {
			filterChain.doFilter(request, response);
		} catch (ServletException | IOException | RuntimeException e) {
			log.debug("[ExceptionHandlerFilter] Filter 단계에서 예외를 공통 핸들러로 전달합니다.");
			exceptionResolver.resolveException(request, response, null, e);
		}
	}
}
