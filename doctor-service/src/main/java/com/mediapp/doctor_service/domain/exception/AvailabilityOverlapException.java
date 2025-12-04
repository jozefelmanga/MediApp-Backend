package com.mediapp.doctor_service.domain.exception;

import java.time.Instant;

import com.mediapp.common.error.DomainException;

/**
 * Raised when a new slot overlaps with existing availability for the doctor.
 */
public class AvailabilityOverlapException extends DomainException {

    public AvailabilityOverlapException(String doctorId, Instant startTime, Instant endTime) {
        super(DoctorErrorCode.AVAILABILITY_OVERLAP,
                "Doctor %s already has availability overlapping %s - %s".formatted(doctorId, startTime, endTime));
    }
}
