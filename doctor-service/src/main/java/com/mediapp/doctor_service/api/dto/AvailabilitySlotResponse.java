package com.mediapp.doctor_service.api.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

/**
 * Response payload describing a single availability slot.
 */
@Builder
public record AvailabilitySlotResponse(
        UUID slotId,
        UUID doctorId,
        Instant startTime,
        Instant endTime,
        boolean reserved) {
}
