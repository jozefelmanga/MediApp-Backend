package com.mediapp.doctor_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediapp.doctor_service.domain.SpecialtyEntity;

/**
 * JPA repository for specialty catalog entries.
 */
@Repository
public interface SpecialtyRepository extends JpaRepository<SpecialtyEntity, Integer> {
}
