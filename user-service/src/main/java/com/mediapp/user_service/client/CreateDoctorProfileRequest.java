package com.mediapp.user_service.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request payload to create a doctor profile in the doctor-service.
 */
public record CreateDoctorProfileRequest(
                @NotBlank @Size(max = 36) String doctorId,
                @NotBlank @Size(max = 50) String medicalLicenseNumber,
                @NotNull @Positive Integer specialtyId,
                @NotBlank @Size(max = 255) String officeAddress) {
}
