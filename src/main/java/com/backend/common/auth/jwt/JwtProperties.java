package com.backend.common.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
	@Valid @NotBlank String secret,
	@Valid @NotNull Long accessTokenExpiration,
	@Valid @NotNull Long refreshTokenExpiration
) {
}
