package com.mediapp.doctor_service.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.mediapp.doctor_service.domain.SpecialtyEntity;

/**
 * Reactive repository for specialty catalog entries.
 */
@Repository
public interface SpecialtyRepository extends ReactiveCrudRepository<SpecialtyEntity, Integer> {
}
