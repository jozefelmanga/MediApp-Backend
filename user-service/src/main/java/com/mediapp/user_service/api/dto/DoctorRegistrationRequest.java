package com.mediapp.user_service.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Incoming payload for creating a doctor account.
 */
public record DoctorRegistrationRequest(
                @NotBlank @Email String email,
                @NotBlank @Size(min = 8, max = 72) @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$", message = "must contain upper, lower case letters and digits") String password,
                @NotBlank @Size(max = 50) String firstName,
                @NotBlank @Size(max = 50) String lastName,
                @NotBlank @Size(max = 50) String medicalLicenseNumber,
                @NotNull @Positive Integer specialtyId,
                @NotBlank @Size(max = 255) String officeAddress) {
}
