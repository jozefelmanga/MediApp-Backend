package com.mediapp.doctor_service.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.mediapp.doctor_service.domain.DoctorProfileEntity;

import reactor.core.publisher.Flux;

/**
 * Reactive repository for doctor profile aggregate roots.
 */
@Repository
public interface DoctorProfileRepository extends ReactiveCrudRepository<DoctorProfileEntity, UUID> {

    Flux<DoctorProfileEntity> findBySpecialtyId(Integer specialtyId);
}
