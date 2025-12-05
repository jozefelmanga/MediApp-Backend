package com.mediapp.booking_service.exception;

/**
 * Exception thrown when an appointment cannot be cancelled.
 */
public class AppointmentCancellationException extends RuntimeException {

    public AppointmentCancellationException(Long appointmentId) {
        super("Cannot cancel appointment: " + appointmentId);
    }

    public AppointmentCancellationException(String message) {
        super(message);
    }
}
