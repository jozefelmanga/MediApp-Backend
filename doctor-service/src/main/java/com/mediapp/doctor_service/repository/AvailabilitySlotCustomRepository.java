package com.mediapp.doctor_service.repository;

import java.time.Instant;
import java.util.UUID;

import com.mediapp.doctor_service.domain.AvailabilitySlotEntity;

import reactor.core.publisher.Mono;

/**
 * Custom operations that require lower level SQL for slot management.
 */
public interface AvailabilitySlotCustomRepository {

    Mono<Boolean> existsOverlappingSlot(UUID doctorId, Instant startTime, Instant endTime);

    Mono<AvailabilitySlotEntity> reserveSlot(UUID slotId, String reservationToken, Instant reservedAt);
}
