package com.mediapp.user_service.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mediapp.user_service.common.dto.BaseDto;
import com.mediapp.user_service.domain.UserRole;

/**
 * Detailed view of a user returned by API endpoints.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDetailsResponse(
        Long userId,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        PatientProfileDto patientProfile) implements BaseDto {
}
