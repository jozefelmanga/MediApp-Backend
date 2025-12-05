package com.mediapp.doctor_service.common.error;

import org.springframework.http.HttpStatus;

/**
 * Common error codes shared across services.
 */
public enum CommonErrorCode implements ErrorCode {

    INTERNAL_ERROR("COMMON-0001", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_FAILED("COMMON-0002", HttpStatus.BAD_REQUEST),
    NOT_FOUND("COMMON-0003", HttpStatus.NOT_FOUND);

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
