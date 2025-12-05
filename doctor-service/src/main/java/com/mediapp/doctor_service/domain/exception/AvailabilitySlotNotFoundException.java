package com.mediapp.doctor_service.domain.exception;

import com.mediapp.doctor_service.common.error.DomainException;

/**
 * Raised when an availability slot cannot be resolved for the provided id.
 */
public class AvailabilitySlotNotFoundException extends DomainException {

    public AvailabilitySlotNotFoundException(Long slotId) {
        super(DoctorErrorCode.AVAILABILITY_NOT_FOUND,
                "Availability slot %d not found".formatted(slotId));
    }
}
