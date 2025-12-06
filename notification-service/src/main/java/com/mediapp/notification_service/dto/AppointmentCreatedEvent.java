package com.mediapp.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCreatedEvent implements Serializable {
    private Long eventId;
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private Long slotId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
}
