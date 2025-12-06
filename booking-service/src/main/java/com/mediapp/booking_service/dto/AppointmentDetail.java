package com.mediapp.booking_service.dto;

import com.mediapp.booking_service.entity.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Response DTO containing appointment details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDetail {

    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private Long slotId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private AppointmentStatus status;
}
