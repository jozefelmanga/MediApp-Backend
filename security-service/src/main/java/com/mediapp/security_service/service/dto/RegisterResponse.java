package com.mediapp.security_service.service.dto;

import com.mediapp.security_service.domain.RoleName;
import java.util.Set;

/**
 * Response payload returned after successful user registration.
 */
public record RegisterResponse(
        Long authUserId,
        String email,
        Set<RoleName> roles) {
}
