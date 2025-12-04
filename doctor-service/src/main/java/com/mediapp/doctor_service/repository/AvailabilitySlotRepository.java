package com.mediapp.doctor_service.repository;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.mediapp.doctor_service.domain.AvailabilitySlotEntity;

import reactor.core.publisher.Flux;

/**
 * Reactive repository for doctor availability slots.
 */
@Repository
public interface AvailabilitySlotRepository
        extends ReactiveCrudRepository<AvailabilitySlotEntity, UUID>, AvailabilitySlotCustomRepository {

    Flux<AvailabilitySlotEntity> findByDoctorIdAndStartTimeBetweenOrderByStartTimeAsc(UUID doctorId,
            Instant startTime,
            Instant endTime);
}
