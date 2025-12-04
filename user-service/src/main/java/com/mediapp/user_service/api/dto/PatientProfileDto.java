package com.mediapp.user_service.api.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Patient-specific details exposed in API responses.
 */
public record PatientProfileDto(
        UUID patientId,
        String phoneNumber,
        LocalDate dateOfBirth) {
}
