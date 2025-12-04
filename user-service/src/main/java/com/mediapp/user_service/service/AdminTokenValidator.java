package com.mediapp.user_service.service;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mediapp.user_service.domain.exception.UserDomainException;
import com.mediapp.user_service.domain.exception.UserErrorCode;

/**
 * Validates that privileged operations are performed with a configured admin
 * token.
 */
@Component
class AdminTokenValidator {

    private final String expectedToken;

    AdminTokenValidator(@Value("${app.user-service.registration.admin-token:change-me}") String expectedToken) {
        this.expectedToken = expectedToken;
    }

    void validate(String providedToken) {
        if (!StringUtils.hasText(providedToken) || !Objects.equals(expectedToken, providedToken)) {
            throw new UserDomainException(UserErrorCode.INVALID_ADMIN_TOKEN, "Invalid administrator token");
        }
    }
}
