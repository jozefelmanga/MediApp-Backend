package com.mediapp.doctor_service.api.dto;

import java.time.Instant;

import lombok.Builder;

/**
 * Response envelope returned after a successful slot reservation.
 */
@Builder
public record SlotReservationResponse(
                Long slotId,
                Long doctorId,
                Instant startTime,
                Instant endTime,
                boolean reserved) {
}
