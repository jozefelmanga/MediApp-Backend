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
 * Response DTO containing appointment details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDetail {

    private UUID appointmentId;
    private UUID patientId;
    private UUID doctorId;
    private String slotId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
