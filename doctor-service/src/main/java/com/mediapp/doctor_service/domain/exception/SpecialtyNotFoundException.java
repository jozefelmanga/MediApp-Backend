package com.mediapp.doctor_service.domain.exception;

import com.mediapp.doctor_service.common.error.DomainException;

/**
 * Raised when the requested specialty identifier does not exist.
 */
public class SpecialtyNotFoundException extends DomainException {

    public SpecialtyNotFoundException(Integer specialtyId) {
        super(DoctorErrorCode.SPECIALTY_NOT_FOUND,
                "Specialty %s not found".formatted(String.valueOf(specialtyId)));
    }
}
