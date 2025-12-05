package com.mediapp.doctor_service.common.error;

import java.util.List;

/**
 * Base exception for domain-specific errors.
 */
public class DomainException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<ErrorDetail> details;

    public DomainException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public DomainException(ErrorCode errorCode, String message, List<ErrorDetail> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public List<ErrorDetail> getDetails() {
        return details;
    }
}
