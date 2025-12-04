package com.mediapp.booking_service.exception;

import com.mediapp.booking_service.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for the booking service.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<ApiError> handleAppointmentNotFound(
            AppointmentNotFoundException ex, HttpServletRequest request) {
        log.warn("Appointment not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(SlotNotAvailableException.class)
    public ResponseEntity<ApiError> handleSlotNotAvailable(
            SlotNotAvailableException ex, HttpServletRequest request) {
        log.warn("Slot not available: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(AppointmentCancellationException.class)
    public ResponseEntity<ApiError> handleAppointmentCancellation(
            AppointmentCancellationException ex, HttpServletRequest request) {
        log.warn("Appointment cancellation failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(DoctorServiceException.class)
    public ResponseEntity<ApiError> handleDoctorServiceException(
            DoctorServiceException ex, HttpServletRequest request) {
        log.error("Doctor service error: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE,
                "Doctor service is temporarily unavailable. Please try again later.", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", message);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.", request);
    }

    private ResponseEntity<ApiError> buildErrorResponse(
            HttpStatus status, String message, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, status);
    }
}
