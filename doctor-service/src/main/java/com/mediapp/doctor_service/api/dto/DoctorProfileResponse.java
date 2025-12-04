package com.mediapp.doctor_service.api.dto;

import java.util.UUID;

import lombok.Builder;

/**
 * Response payload representing a doctor profile entry.
 */
@Builder
public record DoctorProfileResponse(
        UUID doctorId,
        String medicalLicenseNumber,
        Integer specialtyId,
        String specialtyName,
        String officeAddress) {
}
