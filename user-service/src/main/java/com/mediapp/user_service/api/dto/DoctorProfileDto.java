package com.mediapp.user_service.api.dto;

/**
 * Doctor-specific details exposed in API responses.
 */
public record DoctorProfileDto(
        Long doctorId,
        String medicalLicenseNumber,
        Integer specialtyId,
        String specialtyName,
        String officeAddress) {
}
