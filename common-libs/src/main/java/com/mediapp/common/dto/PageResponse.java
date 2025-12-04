package com.mediapp.common.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

/**
 * Standard payload wrapper for paginated resources.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T extends BaseDto> {

    private final List<T> content;
    private final PageMetadata page;

    public static <T extends BaseDto> PageResponse<T> of(List<T> content, PageMetadata metadata) {
        return PageResponse.<T>builder()
                .content(content)
                .page(metadata)
                .build();
    }
}
