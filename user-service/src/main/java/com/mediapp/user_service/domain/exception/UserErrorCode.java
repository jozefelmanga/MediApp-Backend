package com.mediapp.user_service.domain.exception;

import org.springframework.http.HttpStatus;

import com.mediapp.common.error.ErrorCode;

/**
 * User-service specific error catalog.
 */
public enum UserErrorCode implements ErrorCode {

    EMAIL_ALREADY_USED("USR-0001", HttpStatus.CONFLICT),
    USER_NOT_FOUND("USR-0002", HttpStatus.NOT_FOUND),
    INVALID_ADMIN_TOKEN("USR-0003", HttpStatus.FORBIDDEN);

    private final String code;
    private final HttpStatus httpStatus;

    UserErrorCode(String code, HttpStatus httpStatus) {
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
