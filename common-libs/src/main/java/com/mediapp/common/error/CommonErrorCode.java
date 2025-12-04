package com.mediapp.common.error;

import org.springframework.http.HttpStatus;

/**
 * Baseline error codes that cover frequently occurring scenarios across
 * services.
 */
public enum CommonErrorCode implements ErrorCode {

    INTERNAL_ERROR("GEN-0001", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_FAILED("GEN-0002", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("GEN-0003", HttpStatus.NOT_FOUND),
    CONFLICT("GEN-0004", HttpStatus.CONFLICT),
    UNAUTHORIZED("GEN-0005", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("GEN-0006", HttpStatus.FORBIDDEN);

    private final String code;
    private final HttpStatus httpStatus;

    CommonErrorCode(String code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
