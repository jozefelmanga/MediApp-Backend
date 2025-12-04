package com.mediapp.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Specialized error payload for validation problems that pinpoints the field
 * path.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ValidationError(String code, String message, String target, Object rejectedValue) {
}
