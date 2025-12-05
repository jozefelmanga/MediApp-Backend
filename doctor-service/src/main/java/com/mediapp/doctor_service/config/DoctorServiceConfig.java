package com.mediapp.doctor_service.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Core beans shared across the doctor service components.
 */
@Configuration
public class DoctorServiceConfig {

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }
}
