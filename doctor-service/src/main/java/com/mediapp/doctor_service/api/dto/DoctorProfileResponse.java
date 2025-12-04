package com.mediapp.doctor_service.api.dto;

import lombok.Builder;

/**
 * Response payload representing a doctor profile entry.
 */
@Builder
public record DoctorProfileResponse(
                String doctorId,
                String medicalLicenseNumber,
                Integer specialtyId,
                String specialtyName,
                String officeAddress) {
}
