package com.mediapp.doctor_service.domain.exception;

import java.util.UUID;

import com.mediapp.common.error.DomainException;

/**
 * Raised when reserving a slot fails due to an existing reservation.
 */
public class AvailabilityReservationConflictException extends DomainException {

    public AvailabilityReservationConflictException(UUID slotId) {
        super(DoctorErrorCode.AVAILABILITY_CONFLICT,
                "Availability slot %s is already reserved".formatted(slotId));
    }
}
