package com.mediapp.booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for a successful cancellation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationConfirmation {

    private UUID appointmentId;
    private String status;
    private LocalDateTime cancelledAt;
    private String message;
}
