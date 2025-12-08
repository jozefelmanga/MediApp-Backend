package com.mediapp.doctor_service.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Minimal subset of user details returned by user-service that doctor-service
 * needs to enrich responses.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDetailsDto(
        Long userId,
        String email,
        String firstName,
        String lastName) {
}
