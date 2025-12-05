package com.mediapp.user_service.common.dto;

import java.util.List;

/**
 * Standard paginated response wrapper.
 */
public record PageResponse<T>(
        List<T> content,
        PageMetadata page) {

    public static <T> PageResponse<T> of(List<T> content, PageMetadata metadata) {
        return new PageResponse<>(content, metadata);
    }

    public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> page) {
        return new PageResponse<>(page.getContent(), PageMetadata.from(page));
    }

    public static <T, R> PageResponse<R> from(org.springframework.data.domain.Page<T> page, List<R> mappedContent) {
        return new PageResponse<>(mappedContent, PageMetadata.from(page));
    }
}
