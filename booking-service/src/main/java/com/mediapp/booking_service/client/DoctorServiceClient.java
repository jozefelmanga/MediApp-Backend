package com.mediapp.booking_service.client;

import com.mediapp.booking_service.exception.DoctorServiceException;
import com.mediapp.booking_service.exception.SlotNotAvailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * Client for communicating with the doctor-service.
 * Handles slot reservation requests with circuit breaker and retry logic.
 */
@Slf4j
@Component
public class DoctorServiceClient {

    private final RestClient restClient;
    private final String doctorServiceUrl;

    public DoctorServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.doctor-service.url}") String doctorServiceUrl) {
        this.doctorServiceUrl = doctorServiceUrl;
        this.restClient = restClientBuilder
                .baseUrl(doctorServiceUrl)
                .build();
    }

    /**
     * Reserve a slot in the doctor-service.
     * Uses circuit breaker and retry patterns for resilience.
     *
     * @param slotId the slot ID to reserve
     * @return the reservation response
     */
    @CircuitBreaker(name = "doctorService", fallbackMethod = "reserveSlotFallback")
    @Retry(name = "doctorService")
    public SlotReservationResponse reserveSlot(UUID slotId) {
        log.info("Attempting to reserve slot: {} in doctor-service", slotId);

        return restClient.put()
                .uri("/api/v1/doctors/availability/{slotId}/reserve", slotId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    if (response.getStatusCode() == HttpStatus.CONFLICT) {
                        throw new SlotNotAvailableException(slotId);
                    } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                        throw new SlotNotAvailableException("Slot not found: " + slotId);
                    }
                    throw new DoctorServiceException("Client error from doctor-service: " +
                            response.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new DoctorServiceException("Server error from doctor-service: " +
                            response.getStatusCode());
                })
                .body(SlotReservationResponse.class);
    }

    /**
     * Release a previously reserved slot (for compensation/rollback scenarios).
     *
     * @param slotId the slot ID to release
     * @return the reservation response
     */
    @CircuitBreaker(name = "doctorService", fallbackMethod = "releaseSlotFallback")
    @Retry(name = "doctorService")
    public SlotReservationResponse releaseSlot(UUID slotId) {
        log.info("Attempting to release slot: {} in doctor-service", slotId);

        return restClient.put()
                .uri("/api/v1/doctors/availability/{slotId}/release", slotId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    log.warn("Failed to release slot {}: {}", slotId, response.getStatusCode());
                    throw new DoctorServiceException("Failed to release slot: " + slotId);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new DoctorServiceException("Server error from doctor-service: " +
                            response.getStatusCode());
                })
                .body(SlotReservationResponse.class);
    }

    /**
     * Fallback method when doctor-service is unavailable for slot reservation.
     */
    @SuppressWarnings("unused")
    private SlotReservationResponse reserveSlotFallback(UUID slotId, Throwable throwable) {
        log.error("Circuit breaker triggered for reserveSlot. SlotId: {}, Error: {}",
                slotId, throwable.getMessage());
        throw new DoctorServiceException("Doctor service is currently unavailable. " +
                "Please try again later.", throwable);
    }

    /**
     * Fallback method when doctor-service is unavailable for slot release.
     */
    @SuppressWarnings("unused")
    private SlotReservationResponse releaseSlotFallback(UUID slotId, Throwable throwable) {
        log.error("Circuit breaker triggered for releaseSlot. SlotId: {}, Error: {}",
                slotId, throwable.getMessage());
        // Log for manual intervention - slot release is critical
        log.error("CRITICAL: Failed to release slot {}. Manual intervention may be required.", slotId);
        throw new DoctorServiceException("Doctor service is currently unavailable. " +
                "Slot release failed.", throwable);
    }
}
