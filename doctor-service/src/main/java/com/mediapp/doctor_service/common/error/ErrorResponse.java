package com.mediapp.doctor_service.common.error;

import java.time.Instant;
import java.util.List;

/**
 * Standard error response structure for API errors.
 */
public record ErrorResponse(
        String code,
        String message,
        String correlationId,
        Instant timestamp,
        List<ErrorDetail> details) {

    public static ErrorResponse from(ErrorCode errorCode, String message, String correlationId,
            List<ErrorDetail> details) {
        return new ErrorResponse(
                errorCode.code(),
                message,
                correlationId,
                Instant.now(),
                details);
    }
}
