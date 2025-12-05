package com.mediapp.security_service.service;

import com.mediapp.security_service.security.AppUserPrincipal;
import com.mediapp.security_service.service.dto.AuthResponse;
import com.mediapp.security_service.service.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * Coordinates authentication workflows such as issuing JWTs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public AuthResponse authenticate(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException exception) {
            log.debug("Authentication failed for email={}", request.email());
            throw exception;
        }
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        JwtTokenService.AccessToken accessToken = jwtTokenService.issueAccessToken(principal.user());
        return AuthResponse.from(accessToken);
    }
}
