package com.mediapp.user_service.config;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mediapp.user_service.client.SecurityServiceClient;
import com.mediapp.user_service.domain.AppUser;
import com.mediapp.user_service.domain.PatientProfile;
import com.mediapp.user_service.domain.UserRole;
import com.mediapp.user_service.repository.AppUserRepository;
import com.mediapp.user_service.repository.PatientProfileRepository;

/**
 * Inserts deterministic mock data for local development when
 * {@code app.seed.enabled=true}. The runner is idempotent and keeps existing
 * records untouched.
 * 
 * Note: This seeder now coordinates with security-service for authentication
 * data.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DataSeedConfiguration {

        private static final Logger log = LoggerFactory.getLogger(DataSeedConfiguration.class);

        @Bean
        CommandLineRunner userSeedCommandLineRunner(
                        AppUserRepository appUserRepository,
                        PatientProfileRepository patientProfileRepository,
                        SecurityServiceClient securityServiceClient) {
                return args -> {
                        seedUsers(appUserRepository, patientProfileRepository, securityServiceClient);
                };
        }

        private void seedUsers(AppUserRepository appUserRepository,
                        PatientProfileRepository patientProfileRepository,
                        SecurityServiceClient securityServiceClient) {

                List<SeedUserDefinition> definitions = List.of(
                                new SeedUserDefinition(
                                                "admin@mediapp.test",
                                                "AdminPass123",
                                                "Mira",
                                                "Admin",
                                                UserRole.ADMIN,
                                                Optional.empty()),
                                new SeedUserDefinition(
                                                "alice.patient@mediapp.test",
                                                "PatientPwd123",
                                                "Alice",
                                                "Nguyen",
                                                UserRole.PATIENT,
                                                Optional.of(new PatientSeed("+15551230001",
                                                                LocalDate.of(1996, 4, 12)))),
                                new SeedUserDefinition(
                                                "bruno.patient@mediapp.test",
                                                "PatientPwd123",
                                                "Bruno",
                                                "Silva",
                                                UserRole.PATIENT,
                                                Optional.of(new PatientSeed("+15551230002",
                                                                LocalDate.of(1990, 11, 3)))));

                definitions.forEach(definition -> seedSingleUser(
                                definition,
                                appUserRepository,
                                patientProfileRepository,
                                securityServiceClient));
        }

        private void seedSingleUser(SeedUserDefinition definition,
                        AppUserRepository appUserRepository,
                        PatientProfileRepository patientProfileRepository,
                        SecurityServiceClient securityServiceClient) {

                if (appUserRepository.existsByEmailIgnoreCase(definition.email())) {
                        log.debug("User seed skipped because email already exists: {}", definition.email());
                        return;
                }

                try {
                        // 1. Register in security-service first (single source of truth for auth)
                        SecurityServiceClient.RegisterResponse authResponse = securityServiceClient.registerUser(
                                        definition.email(), definition.rawPassword(), definition.role());

                        // 2. Create profile in user-service (no password stored here)
                        AppUser user = AppUser.builder()
                                        .authUserId(authResponse.authUserId())
                                        .email(definition.email())
                                        .firstName(definition.firstName())
                                        .lastName(definition.lastName())
                                        .role(definition.role())
                                        .build();

                        AppUser persistedUser = appUserRepository.save(user);

                        definition.patientSeed().ifPresent(patientSeed -> {
                                PatientProfile profile = PatientProfile.create(
                                                persistedUser,
                                                patientSeed.phoneNumber(),
                                                patientSeed.dateOfBirth());
                                patientProfileRepository.save(profile);
                        });

                        log.info("Seeded user account: {} (role={}, authUserId={})",
                                        definition.email(), definition.role(), authResponse.authUserId());
                } catch (Exception e) {
                        log.warn("Initial register failed for {}: {}. Attempting lookup...", definition.email(),
                                        e.getMessage());
                        // Try to lookup existing auth user in security-service and import
                        try {
                                SecurityServiceClient.RegisterResponse existing = securityServiceClient
                                                .lookupUserByEmail(definition.email());
                                if (existing != null) {
                                        AppUser user = AppUser.builder()
                                                        .authUserId(existing.authUserId())
                                                        .email(definition.email())
                                                        .firstName(definition.firstName())
                                                        .lastName(definition.lastName())
                                                        .role(definition.role())
                                                        .build();
                                        AppUser persistedUser = appUserRepository.save(user);
                                        definition.patientSeed().ifPresent(patientSeed -> {
                                                PatientProfile profile = PatientProfile.create(
                                                                persistedUser,
                                                                patientSeed.phoneNumber(),
                                                                patientSeed.dateOfBirth());
                                                patientProfileRepository.save(profile);
                                        });
                                        log.info("Imported existing auth user into user-service: {} (authUserId={})",
                                                        definition.email(), existing.authUserId());
                                } else {
                                        log.warn("Failed to seed user {}: no auth user found in security-service",
                                                        definition.email());
                                }
                        } catch (Exception ex) {
                                log.warn("Failed to seed user {} after lookup attempt: {}", definition.email(),
                                                ex.getMessage());
                        }
                }
        }

        private record SeedUserDefinition(
                        String email,
                        String rawPassword,
                        String firstName,
                        String lastName,
                        UserRole role,
                        Optional<PatientSeed> patientSeed) {
        }

        private record PatientSeed(String phoneNumber, LocalDate dateOfBirth) {
        }
}
