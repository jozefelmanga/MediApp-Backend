package com.mediapp.security_service.service.dto;

import com.mediapp.security_service.domain.RefreshToken;
import com.mediapp.security_service.service.JwtTokenService;
import java.time.Instant;

public record AuthResponse(
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        String tokenType) {

    public AuthResponse {
        tokenType = tokenType == null ? "Bearer" : tokenType;
    }

    public static AuthResponse from(JwtTokenService.AccessToken accessToken, RefreshToken refreshToken) {
        return new AuthResponse(
                accessToken.value(),
                accessToken.expiresAt(),
                refreshToken.getToken(),
                refreshToken.getExpiresAt(),
                "Bearer");
    }
}
