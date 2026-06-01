package com.backend.common.config;

import java.util.List;

import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;

/**
 * Swagger OpenAPI 설정
 * - JWT Bearer Token 인증 (Authorization Header)
 * - RefreshToken은 브라우저가 자동으로 쿠키에서 전송 (withCredentials: true)
 */
@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

	private final Environment env;

	@Bean
	public OpenAPI customOpenAPI() {

		// 현재 활성화된 profile 가져오기
		String profile = env.getActiveProfiles().length > 0 ? env.getActiveProfiles()[0] : "local";

		// JWT Access Token 인증 스킴 (Authorization Header)
		SecurityScheme bearerAuth = new SecurityScheme()
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
			.in(SecurityScheme.In.HEADER)
			.name("Authorization")
			.description("JWT Access Token (Bearer {token})");

		// Security Requirement
		SecurityRequirement securityRequirement = new SecurityRequirement()
			.addList("bearerAuth");

		Server server = new Server();
		if (profile.equals("dev")) {
			server.setUrl("https://comeet.kro.kr");
			server.setDescription("개발 서버");
		} else {
			server.setUrl("http://localhost:8080");
			server.setDescription("로컬 서버");
		}

		return new OpenAPI()
			.info(new Info()
				.title("Comeet API")
				.description("커피 탐방 플랫폼 Comeet API 문서")
				.version("v1.0.0"))
			.servers(List.of(server))
			.components(new Components()
				.addSecuritySchemes("bearerAuth", bearerAuth))
			.addSecurityItem(securityRequirement);
	}

	/**
	 * Swagger UI 설정
	 * - Authorization 정보 브라우저에 저장 (persistAuthorization)
	 * - 쿠키 자동 전송 활성화
	 */
	@Bean
	@Primary
	public SwaggerUiConfigProperties swaggerUiConfigProperties(SwaggerUiConfigProperties props) {
		// Authorization 정보를 브라우저에 유지 (새로고침 시에도 유지)
		props.setPersistAuthorization(true);

		// 쿠키 전송 활성화 (withCredentials: true)
		props.setWithCredentials(true);

		return props;
	}
}
