package com.mediapp.doctor_service.domain.exception;

import java.util.UUID;

import com.mediapp.common.error.DomainException;

/**
 * Raised when an availability slot cannot be resolved for the provided id.
 */
public class AvailabilitySlotNotFoundException extends DomainException {

    public AvailabilitySlotNotFoundException(UUID slotId) {
        super(DoctorErrorCode.AVAILABILITY_NOT_FOUND,
                "Availability slot %s not found".formatted(slotId));
    }
}
