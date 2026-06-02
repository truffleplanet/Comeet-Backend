package com.backend.common.sqids.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sqids.Sqids;
import lombok.extern.slf4j.Slf4j;

/**
 * 환경 변수(env)로부터 알파벳과 최소 문자 길이를 주입받아 Sqids 빈을 설정합니다.
 */
@Slf4j
@Configuration
public class SqidsConfig {

	@Value("${sqids.alphabet}")
	private String alphabet;

	@Value("${sqids.min-length}")
	private int minLength;

	@Bean
	public Sqids sqids() {
		log.info("[SqidsConfig] Sqids 인스턴스 초기화 중 (최소길이: {})", minLength);
		try {
			return Sqids.builder()
				.alphabet(alphabet)
				.minLength(minLength)
				.build();
		} catch (Exception e) {
			log.error("[SqidsConfig] Sqids 빌드 에러 - 알파벳 구성을 확인해주세요.", e);
			throw new IllegalStateException("Sqids 빌드 실패", e);
		}
	}
}
