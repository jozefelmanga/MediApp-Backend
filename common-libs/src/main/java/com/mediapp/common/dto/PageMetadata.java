package com.mediapp.common.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Encapsulates pagination metadata that appears in paged API responses.
 */
@Getter
@Builder
public class PageMetadata {

    private final long totalElements;
    private final int totalPages;
    private final int pageNumber;
    private final int pageSize;
}
