package com.mediapp.doctor_service.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mediapp.doctor_service.domain.SpecialtyEntity;
import com.mediapp.doctor_service.repository.SpecialtyRepository;

/**
 * Seeds immutable reference data required by the doctor-service when
 * {@code app.seed.enabled=true}.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DataSeedConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DataSeedConfiguration.class);

    @Bean
    CommandLineRunner specialtySeedCommandLineRunner(SpecialtyRepository specialtyRepository) {
        return args -> seedSpecialties(specialtyRepository);
    }

    private void seedSpecialties(SpecialtyRepository specialtyRepository) {
        List<SpecialtyEntity> specialties = List.of(
                SpecialtyEntity.builder().id(101).name("Cardiology").build(),
                SpecialtyEntity.builder().id(102).name("Dermatology").build(),
                SpecialtyEntity.builder().id(103).name("Pediatrics").build(),
                SpecialtyEntity.builder().id(104).name("Orthopedics").build());

        specialties.forEach(specialty -> {
            if (specialtyRepository.existsById(specialty.getId())) {
                return;
            }
            specialtyRepository.save(specialty);
            log.info("Seeded specialty {} ({})", specialty.getId(), specialty.getName());
        });
    }
}
