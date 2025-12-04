package com.mediapp.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCreatedEvent implements Serializable {
    private UUID eventId;
    private UUID appointmentId;
    private UUID patientId;
    private UUID doctorId;
    private UUID slotId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalDateTime createdAt;
}
