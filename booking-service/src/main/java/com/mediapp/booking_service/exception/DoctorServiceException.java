package com.mediapp.booking_service.exception;

/**
 * Exception thrown when there's an issue communicating with the doctor service.
 */
public class DoctorServiceException extends RuntimeException {

    public DoctorServiceException(String message) {
        super(message);
    }

    public DoctorServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
