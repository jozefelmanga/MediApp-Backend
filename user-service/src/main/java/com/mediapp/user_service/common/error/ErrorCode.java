package com.mediapp.user_service.common.error;

import org.springframework.http.HttpStatus;

/**
 * Contract for error codes used throughout the application.
 */
public interface ErrorCode {

    String code();

    HttpStatus httpStatus();

    default String messageKey() {
        return "error." + code();
    }
}
