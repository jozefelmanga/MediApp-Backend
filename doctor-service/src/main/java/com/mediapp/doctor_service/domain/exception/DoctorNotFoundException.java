package com.mediapp.doctor_service.domain.exception;

import com.mediapp.doctor_service.common.error.DomainException;

/**
 * Raised when a doctor profile cannot be found for a given identifier.
 */
public class DoctorNotFoundException extends DomainException {

    public DoctorNotFoundException(Long doctorId) {
        super(DoctorErrorCode.DOCTOR_NOT_FOUND, "Doctor profile %d not found".formatted(doctorId));
    }
}
