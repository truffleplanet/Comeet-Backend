package com.backend.common.sqids.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.backend.common.sqids.domain.Sqid;
import com.backend.common.sqids.service.IdObfuscator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC 환경에서 HTTP 요청 매개변수(PathVariable, RequestParam)에 있는
 * Sqids 문자열 식별자를 Sqid 객체로 자동 디코딩하는 웹 설정 클래스입니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SqidWebMvcConfig implements WebMvcConfigurer {

	private final IdObfuscator idObfuscator;

	@Override
	public void addFormatters(FormatterRegistry registry) {
		log.info("[SqidWebMvcConfig] Sqid 문자열 -> Sqid 객체 포맷터 등록 완료");
		registry.addConverter(new Converter<String, Sqid>() {
			@Override
			public Sqid convert(String source) {
				if (source == null || source.isBlank()) {
					return null;
				}
				return Sqid.of(idObfuscator.decode(source));
			}
		});
		registry.addConverter(new Converter<String, Long>() {
			@Override
			public Long convert(String source) {
				if (source == null || source.isBlank()) {
					return null;
				}
				return idObfuscator.decode(source);
			}
		});
	}
}
