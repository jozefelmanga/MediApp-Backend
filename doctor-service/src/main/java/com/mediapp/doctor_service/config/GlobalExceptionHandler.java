package com.mediapp.doctor_service.config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mediapp.doctor_service.common.error.CommonErrorCode;
import com.mediapp.doctor_service.common.error.DomainException;
import com.mediapp.doctor_service.common.error.ErrorDetail;
import com.mediapp.doctor_service.common.error.ErrorResponse;

/**
 * Global exception handler for Spring MVC controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex) {
        String correlationId = UUID.randomUUID().toString();
        ErrorResponse response = ErrorResponse.from(
                ex.getErrorCode(),
                ex.getMessage(),
                correlationId,
                ex.getDetails());
        return ResponseEntity.status(ex.getErrorCode().httpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String correlationId = UUID.randomUUID().toString();
        List<ErrorDetail> details = new ArrayList<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            details.add(new ErrorDetail(
                    CommonErrorCode.VALIDATION_FAILED.code(),
                    fieldError.getDefaultMessage(),
                    fieldError.getField(),
                    fieldError.getRejectedValue()));
        }

        ErrorResponse response = ErrorResponse.from(
                CommonErrorCode.VALIDATION_FAILED,
                "Validation failed",
                correlationId,
                details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String correlationId = UUID.randomUUID().toString();
        ErrorResponse response = ErrorResponse.from(
                CommonErrorCode.INTERNAL_ERROR,
                ex.getMessage(),
                correlationId,
                null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
