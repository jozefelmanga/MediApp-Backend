package com.mediapp.doctor_service.api.dto;

import java.time.Instant;

import lombok.Builder;

/**
 * Response payload describing a single availability slot.
 */
@Builder
public record AvailabilitySlotResponse(
                String slotId,
                String doctorId,
                Instant startTime,
                Instant endTime,
                boolean reserved) {
}
