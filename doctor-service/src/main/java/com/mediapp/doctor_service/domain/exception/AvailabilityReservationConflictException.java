package com.mediapp.doctor_service.domain.exception;

import com.mediapp.doctor_service.common.error.DomainException;

/**
 * Raised when reserving a slot fails due to an existing reservation.
 */
public class AvailabilityReservationConflictException extends DomainException {

    public AvailabilityReservationConflictException(Long slotId) {
        super(DoctorErrorCode.AVAILABILITY_CONFLICT,
                "Availability slot %d is already reserved".formatted(slotId));
    }
}
