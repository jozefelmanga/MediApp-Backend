package com.mediapp.doctor_service.api.dto;

import lombok.Builder;

/**
 * Response payload representing a doctor profile entry.
 */
@Builder
public record DoctorProfileResponse(
                Long doctorId,
                Long userId,
                String medicalLicenseNumber,
                Integer specialtyId,
                String specialtyName,
                String officeAddress,
                String email,
                String firstName,
                String lastName) {
}
