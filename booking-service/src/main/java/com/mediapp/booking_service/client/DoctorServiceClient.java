package com.mediapp.booking_service.client;

import com.mediapp.booking_service.exception.DoctorServiceException;
import com.mediapp.booking_service.exception.SlotNotAvailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

/**
 * Client for communicating with the doctor-service.
 * Handles slot reservation requests.
 */
@Slf4j
@Component
public class DoctorServiceClient {

    private final RestClient restClient;

    public DoctorServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.doctor-service.url}") String doctorServiceUrl) {
        this.restClient = restClientBuilder
                .baseUrl(doctorServiceUrl)
                .build();
    }

    /**
     * Reserve a slot in the doctor-service.
     *
     * @param slotId the slot ID to reserve
     * @return the reservation response
     */
    public SlotReservationResponse reserveSlot(Long slotId) {
        log.info("Attempting to reserve slot: {} in doctor-service", slotId);

        // Generate a unique reservation token for this booking
        String reservationToken = UUID.randomUUID().toString();

        DoctorServiceApiResponse<SlotReservationResponse> apiResponse = restClient.put()
                .uri("/api/v1/doctors/availability/{slotId}/reserve", slotId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("reservationToken", reservationToken))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    if (response.getStatusCode() == HttpStatus.CONFLICT) {
                        throw SlotNotAvailableException.forSlot(slotId);
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
                .body(new org.springframework.core.ParameterizedTypeReference<DoctorServiceApiResponse<SlotReservationResponse>>() {
                });

        return apiResponse != null ? apiResponse.getData() : null;
    }

    /**
     * Release a previously reserved slot (for compensation/rollback scenarios).
     *
     * @param slotId the slot ID to release
     * @return the reservation response
     */
    public SlotReservationResponse releaseSlot(Long slotId) {
        log.info("Attempting to release slot: {} in doctor-service", slotId);

        DoctorServiceApiResponse<SlotReservationResponse> apiResponse = restClient.put()
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
                .body(new org.springframework.core.ParameterizedTypeReference<DoctorServiceApiResponse<SlotReservationResponse>>() {
                });

        return apiResponse != null ? apiResponse.getData() : null;
    }
}
