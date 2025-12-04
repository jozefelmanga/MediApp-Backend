package com.mediapp.common.error;

import org.springframework.http.HttpStatus;

/**
 * Shared contract for application error codes so that every service exposes
 * consistent metadata.
 */
public interface ErrorCode {

    String code();

    HttpStatus httpStatus();

    default String messageKey() {
        return "error." + code().toLowerCase().replace('-', '.');
    }
}
