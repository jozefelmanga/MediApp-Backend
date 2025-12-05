package com.mediapp.user_service.common.error;

import java.util.List;

/**
 * Base exception for domain-specific errors.
 */
public class DomainException extends RuntimeException {

    private final ErrorCode errorCode;
    private final List<Object> details;

    public DomainException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public DomainException(ErrorCode errorCode, String message, List<Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public List<Object> getDetails() {
        return details;
    }
}
