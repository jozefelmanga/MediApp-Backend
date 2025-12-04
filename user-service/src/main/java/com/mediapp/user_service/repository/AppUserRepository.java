package com.mediapp.user_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mediapp.user_service.domain.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "patientProfile")
    Optional<AppUser> findWithPatientProfileById(UUID id);
}
