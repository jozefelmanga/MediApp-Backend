package com.mediapp.booking_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Event published when a new appointment is created.
 * Consumed by notification-service to send confirmation notifications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long eventId;
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private Long slotId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
}
