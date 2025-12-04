package com.mediapp.doctor_service.api.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

/**
 * Response envelope returned after a successful slot reservation.
 */
@Builder
public record SlotReservationResponse(
        UUID slotId,
        UUID doctorId,
        Instant startTime,
        Instant endTime,
        Instant reservedAt,
        String reservationToken) {
}
