package com.mediapp.user_service.api.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.mediapp.user_service.common.dto.BaseDto;

/**
 * Lightweight projection for patient listings.
 */
public record PatientSummaryDto(
                UUID patientId,
                String email,
                String firstName,
                String lastName,
                String phoneNumber,
                LocalDate dateOfBirth) implements BaseDto {
}
