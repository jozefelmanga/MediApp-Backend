package com.mediapp.common.error;

import java.util.List;

import lombok.Getter;

/**
 * Base runtime exception that carries a shared error code and optional error
 * details.
 */
@Getter
public class DomainException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient List<ErrorDetail> details;

    public DomainException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }

    public DomainException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = null;
    }

    public DomainException(ErrorCode errorCode, String message, List<ErrorDetail> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }
}
