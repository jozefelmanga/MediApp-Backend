package com.mediapp.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mediapp.user_service.domain.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "patientProfile")
    Optional<AppUser> findWithPatientProfileById(Long id);
}
