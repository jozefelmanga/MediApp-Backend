package com.mediapp.doctor_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mediapp.doctor_service.domain.DoctorProfileEntity;

/**
 * JPA repository for doctor profile aggregate roots.
 */
@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfileEntity, Long> {

    List<DoctorProfileEntity> findBySpecialtyId(Integer specialtyId);

    Optional<DoctorProfileEntity> findByUserId(Long userId);
}
