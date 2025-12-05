package com.mediapp.doctor_service.common.error;

/**
 * Represents a single error detail within a validation or domain error.
 */
public record ErrorDetail(
        String code,
        String message,
        String field,
        Object rejectedValue) {

    public ErrorDetail(String code, String message, String field) {
        this(code, message, field, null);
    }
}
