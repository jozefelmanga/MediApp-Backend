package com.mediapp.booking_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Request DTO for booking an appointment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;

    @NotNull(message = "Slot ID is required")
    private UUID slotId;

    @NotNull(message = "Appointment date is required")
    private LocalDate appointmentDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;
}
