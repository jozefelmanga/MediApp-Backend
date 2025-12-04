package com.mediapp.user_service.domain.exception;

import com.mediapp.common.error.DomainException;

/**
 * Convenience exception that pins a {@link UserErrorCode} to user-service
 * failures.
 */
public class UserDomainException extends DomainException {

    public UserDomainException(UserErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
