package com.mediapp.booking_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Event published when an appointment is cancelled.
 * Consumed by notification-service to send cancellation notifications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentCancelledEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID eventId;
    private UUID appointmentId;
    private UUID patientId;
    private UUID doctorId;
    private String slotId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalDateTime cancelledAt;
    private String reason;
}
