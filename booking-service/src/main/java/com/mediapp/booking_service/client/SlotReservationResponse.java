package com.mediapp.booking_service.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a slot update response from doctor-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotReservationResponse {

    private UUID slotId;
    private UUID doctorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean reserved;
    private String message;
}
