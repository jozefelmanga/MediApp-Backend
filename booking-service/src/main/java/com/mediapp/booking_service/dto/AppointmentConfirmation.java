package com.mediapp.booking_service.dto;

import com.mediapp.booking_service.entity.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Response DTO for a successful booking confirmation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentConfirmation {

    private UUID appointmentId;
    private UUID patientId;
    private UUID doctorId;
    private UUID slotId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private AppointmentStatus status;
    private LocalDateTime confirmedAt;
    private String message;
}
