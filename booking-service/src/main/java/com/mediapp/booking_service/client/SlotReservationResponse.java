package com.mediapp.booking_service.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO representing a slot reservation response from doctor-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotReservationResponse {

    private Long slotId;
    private Long doctorId;
    private Instant startTime;
    private Instant endTime;
    private Instant reservedAt;
    private String reservationToken;
}
