package com.mediapp.doctor_service.domain.exception;

import com.mediapp.common.error.DomainException;

/**
 * Raised when an availability slot cannot be resolved for the provided id.
 */
public class AvailabilitySlotNotFoundException extends DomainException {

    public AvailabilitySlotNotFoundException(String slotId) {
        super(DoctorErrorCode.AVAILABILITY_NOT_FOUND,
                "Availability slot %s not found".formatted(slotId));
    }
}
