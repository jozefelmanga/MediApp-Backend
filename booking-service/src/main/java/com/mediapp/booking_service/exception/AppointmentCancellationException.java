package com.mediapp.booking_service.exception;

import java.util.UUID;

/**
 * Exception thrown when an appointment cannot be cancelled.
 */
public class AppointmentCancellationException extends RuntimeException {

    public AppointmentCancellationException(UUID appointmentId) {
        super("Cannot cancel appointment: " + appointmentId);
    }

    public AppointmentCancellationException(String message) {
        super(message);
    }
}
