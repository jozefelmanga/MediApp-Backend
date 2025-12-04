package com.mediapp.booking_service.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a slot update response from doctor-service.
 * Uses String for IDs since doctor-service returns String IDs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotReservationResponse {

    private String slotId;
    private String doctorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean reserved;
    private String message;
}
