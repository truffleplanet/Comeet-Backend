package com.backend.common.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.backend.common.filter.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CorsConfig corsConfig;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	private static final String[] WHITELIST = {
		// Web
		"/",
		"/error",
		"/favicon.ico",

		// Swagger
		"/v3/api-docs/**",
		"/swagger-ui/**",
		"/swagger-resources/**",

		// Actuator
		"/actuator",
		"/actuator/**",

		// Auth
		"/auth/signup",
		"/auth/login",
		"/auth/reissue",

		// Local development
		"/dev/**",
	};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.sessionManagement(SecurityConfig::createSessionPolicy);

		http
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
				.requestMatchers(WHITELIST).permitAll()

				// 목록 조회 API (GET only)
				.requestMatchers(HttpMethod.GET, "/stores").permitAll()
				.requestMatchers(HttpMethod.GET, "/beans").permitAll()
				.requestMatchers(HttpMethod.GET, "/flavors").permitAll()

				// 숫자 ID 기반 상세 조회 API (GET only)
				// {id:\\d+} 패턴으로 숫자만 매칭, /stores/my 같은 경로는 authenticated로 처리
				.requestMatchers(HttpMethod.GET, "/stores/{id:\\d+}").permitAll()
				.requestMatchers(HttpMethod.GET, "/stores/{id:\\d+}/menus").permitAll()
				.requestMatchers(HttpMethod.GET, "/stores/{id:\\d+}/reviews").permitAll()
				.requestMatchers(HttpMethod.GET, "/menus/{id:\\d+}").permitAll()
				.requestMatchers(HttpMethod.GET, "/beans/{id:\\d+}").permitAll()

				.anyRequest().authenticated()
			);

		http
			.addFilterBefore(corsConfig.corsFilter(), UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	private static void createSessionPolicy(SessionManagementConfigurer<HttpSecurity> session) {
		session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return username -> {
			throw new UsernameNotFoundException(username);
		};
	}
}
