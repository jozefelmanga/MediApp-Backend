package com.mediapp.common.error;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

/**
 * Normalized error envelope that wraps domain and validation issues.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String requestId;
    private final Instant timestamp;
    private final String code;
    private final String message;
    private final List<ErrorDetail> details;

    public static ErrorResponse from(ErrorCode errorCode, String message, String requestId, List<ErrorDetail> details) {
        return ErrorResponse.builder()
                .code(errorCode.code())
                .message(message)
                .requestId(requestId)
                .timestamp(Instant.now())
                .details(details)
                .build();
    }

    public static ErrorResponse from(ErrorCode errorCode, String message) {
        return from(errorCode, message, null, null);
    }
}
