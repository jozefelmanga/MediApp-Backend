package com.mediapp.doctor_service.domain.exception;

import org.springframework.http.HttpStatus;

import com.mediapp.common.error.ErrorCode;

/**
 * Error codes specific to the doctor service domain.
 */
public enum DoctorErrorCode implements ErrorCode {

    DOCTOR_NOT_FOUND("DOC-0001", HttpStatus.NOT_FOUND),
    SPECIALTY_NOT_FOUND("DOC-0002", HttpStatus.NOT_FOUND),
    AVAILABILITY_NOT_FOUND("DOC-0003", HttpStatus.NOT_FOUND),
    AVAILABILITY_CONFLICT("DOC-0004", HttpStatus.CONFLICT),
    AVAILABILITY_OVERLAP("DOC-0005", HttpStatus.CONFLICT);

    private final String code;
    private final HttpStatus httpStatus;

    DoctorErrorCode(String code, HttpStatus httpStatus) {
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
