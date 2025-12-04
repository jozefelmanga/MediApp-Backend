package com.mediapp.security_service.service;

import com.mediapp.security_service.config.properties.JwtProperties;
import com.mediapp.security_service.domain.AppUser;
import com.mediapp.security_service.domain.RefreshToken;
import com.mediapp.security_service.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Manages persistence and validation of refresh tokens.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public RefreshToken create(AppUser user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(generateTokenValue())
                .expiresAt(Instant.now().plus(jwtProperties.getRefreshTokenTtl()))
                .user(user)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken rotate(String tokenValue) {
        RefreshToken existing = validateActiveToken(tokenValue);
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);
        return create(existing.getUser());
    }

    @Transactional(readOnly = true)
    public RefreshToken validateActiveToken(String tokenValue) {
        Instant now = Instant.now();
        return refreshTokenRepository.findByToken(tokenValue)
                .filter(token -> !token.isRevoked())
                .filter(token -> token.getExpiresAt().isAfter(now))
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token"));
    }

    @Transactional
    public void purgeExpiredTokens() {
        Instant cutoff = Instant.now();
        refreshTokenRepository.deleteExpiredTokens(cutoff);
        log.debug("Purged refresh tokens expired before {}", cutoff);
    }

    private String generateTokenValue() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
