package com.mediapp.security_service.bootstrap;

import com.mediapp.security_service.domain.AppUser;
import com.mediapp.security_service.domain.RoleName;
import com.mediapp.security_service.repository.AppUserRepository;
import java.util.EnumSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds test users for development and testing purposes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run after AdminBootstrapRunner
public class TestDataSeeder implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        createTestUserIfNotExists(
                "patient@mediapp.com",
                "Patient123",
                RoleName.PATIENT,
                "Test Patient");

        createTestUserIfNotExists(
                "doctor@mediapp.com",
                "Doctor123",
                RoleName.DOCTOR,
                "Test Doctor");
    }

    private void createTestUserIfNotExists(String email, String password, RoleName role, String description) {
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            log.debug("{} already exists: {}", description, email);
            return;
        }

        AppUser user = AppUser.builder()
                .email(email.toLowerCase())
                .passwordHash(passwordEncoder.encode(password))
                .roles(EnumSet.of(role))
                .enabled(true)
                .build();

        appUserRepository.save(user);
        log.info("✅ {} created: {} / {}", description, email, password);
    }
}
