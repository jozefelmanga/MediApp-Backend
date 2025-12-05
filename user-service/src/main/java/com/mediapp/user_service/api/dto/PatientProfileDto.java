package com.mediapp.user_service.api.dto;

import java.time.LocalDate;

/**
 * Patient-specific details exposed in API responses.
 */
public record PatientProfileDto(
                Long patientId,
                String phoneNumber,
                LocalDate dateOfBirth) {
}
