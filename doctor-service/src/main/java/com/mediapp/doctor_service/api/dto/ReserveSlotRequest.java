package com.mediapp.doctor_service.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * Command payload to reserve an availability slot via internal API.
 */
@Builder
public record ReserveSlotRequest(@NotBlank String reservationToken) {
}
