package com.mediapp.doctor_service.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request payload to create a doctor profile from user-service.
 */
public record CreateDoctorProfileRequest(
        @NotNull @Positive Long userId,
        @NotBlank @Size(max = 50) String medicalLicenseNumber,
        @NotNull @Positive Integer specialtyId,
        @NotBlank @Size(max = 255) String officeAddress) {
}
