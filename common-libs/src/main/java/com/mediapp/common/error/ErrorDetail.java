package com.mediapp.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Provides structured information about a single error occurrence.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(String code, String message, String target, Object rejectedValue) {
    public ErrorDetail(String code, String message, String target) {
        this(code, message, target, null);
    }
}
