package com.mediapp.common.dto;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

/**
 * Canonical API envelope used across services to simplify integration.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final String requestId;
    private final Instant timestamp;
    private final T data;
    private final Map<String, Object> metadata;

    public static <T> ApiResponse<T> success(String requestId, T data, Map<String, Object> metadata) {
        return ApiResponse.<T>builder()
                .requestId(requestId)
                .timestamp(Instant.now())
                .data(data)
                .metadata(metadata)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(null, data, null);
    }
}
