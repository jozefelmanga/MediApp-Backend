package com.mediapp.booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for a successful cancellation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationConfirmation {

    private Long appointmentId;
    private String status;
    private LocalDateTime cancelledAt;
    private String message;
}
