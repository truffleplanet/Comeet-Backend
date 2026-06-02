package com.backend.common.sqids.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.sqids.Sqids;

import com.backend.common.error.ErrorCode;
import com.backend.common.error.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

/**
 * Sqids 알고리즘을 사용한 ID 암·복호화(난독화) 유틸리티 서비스.
 */
@Slf4j
@Component
public class IdObfuscator {

	private static volatile IdObfuscator instance;

	private final Sqids sqids;
	private final int minLength;

	public IdObfuscator(Sqids sqids, @Value("${sqids.min-length}") int minLength) {
		this.sqids = sqids;
		this.minLength = minLength;
		instance = this;
	}

	public static IdObfuscator getInstance() {
		return instance;
	}

	public static IdObfuscator getRequiredInstance() {
		if (instance == null) {
			throw new IllegalStateException("Sqids obfuscator has not been initialized.");
		}
		return instance;
	}

	/**
	 * 숫자 ID를 난독화된 Sqids 문자열로 인코딩합니다.
	 */
	public String encode(Long id) {
		if (id == null) {
			return null;
		}
		try {
			return sqids.encode(List.of(id));
		} catch (Exception e) {
			log.error("[Sqids] ID 인코딩 실패 - id: {}", id, e);
			throw new IllegalStateException("ID 인코딩에 실패했습니다.", e);
		}
	}

	/**
	 * 난독화된 Sqids 문자열을 숫자 ID로 디코딩합니다.
	 */
	public Long decode(String code) {
		if (code == null || code.isBlank()) {
			return null;
		}
		if (code.length() < minLength) {
			log.warn("[Sqids] 최소 길이보다 짧은 식별자 인입 - code: {}", code);
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
		try {
			List<Long> numbers = sqids.decode(code);
			if (numbers.isEmpty()) {
				log.warn("[Sqids] 디코딩 결과가 비어있음 - code: {}", code);
				throw new BusinessException(ErrorCode.INVALID_INPUT);
			}
			return numbers.get(0);
		} catch (Exception e) {
			log.error("[Sqids] ID 디코딩 실패 - code: {}", code, e);
			if (e instanceof BusinessException businessException) {
				throw businessException;
			}
			throw new BusinessException(ErrorCode.INVALID_INPUT);
		}
	}
}
