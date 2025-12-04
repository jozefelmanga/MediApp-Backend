package com.mediapp.booking_service.exception;

import java.util.UUID;

/**
 * Exception thrown when an appointment is not found.
 */
public class AppointmentNotFoundException extends RuntimeException {

    public AppointmentNotFoundException(UUID appointmentId) {
        super("Appointment not found with ID: " + appointmentId);
    }

    public AppointmentNotFoundException(String message) {
        super(message);
    }
}
