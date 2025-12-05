package com.mediapp.booking_service.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO representing a slot reservation response from doctor-service.
 * Uses String for IDs since doctor-service returns String IDs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotReservationResponse {

    private String slotId;
    private String doctorId;
    private Instant startTime;
    private Instant endTime;
    private Instant reservedAt;
    private String reservationToken;
}
