package com.mediapp.doctor_service.domain.exception;

import java.util.UUID;

import com.mediapp.common.error.DomainException;

/**
 * Raised when a doctor profile cannot be found for a given identifier.
 */
public class DoctorNotFoundException extends DomainException {

    public DoctorNotFoundException(UUID doctorId) {
        super(DoctorErrorCode.DOCTOR_NOT_FOUND, "Doctor profile %s not found".formatted(doctorId));
    }
}
