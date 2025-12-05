package com.mediapp.booking_service.exception;

/**
 * Exception thrown when an appointment is not found.
 */
public class AppointmentNotFoundException extends RuntimeException {

    public AppointmentNotFoundException(Long appointmentId) {
        super("Appointment not found with ID: " + appointmentId);
    }

    public AppointmentNotFoundException(String message) {
        super(message);
    }
}
