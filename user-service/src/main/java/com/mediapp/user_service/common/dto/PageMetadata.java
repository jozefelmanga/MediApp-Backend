package com.mediapp.user_service.common.dto;

import lombok.Builder;

/**
 * Pagination metadata for paginated responses.
 */
@Builder
public record PageMetadata(
        long totalElements,
        int totalPages,
        int pageNumber,
        int pageSize) {

    public static PageMetadata from(org.springframework.data.domain.Page<?> page) {
        return PageMetadata.builder()
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .build();
    }
}
