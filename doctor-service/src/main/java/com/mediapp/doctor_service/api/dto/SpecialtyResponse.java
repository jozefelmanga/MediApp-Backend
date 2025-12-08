package com.mediapp.doctor_service.api.dto;

import lombok.Builder;

/**
 * Response payload for a medical specialty.
 */
@Builder
public record SpecialtyResponse(
        Integer specialtyId,
        String name) {
}
