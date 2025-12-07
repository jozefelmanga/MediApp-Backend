package com.mediapp.booking_service.client;

import com.mediapp.booking_service.exception.DoctorServiceException;
import com.mediapp.booking_service.exception.SlotNotAvailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Client for communicating with the doctor-service using WebClient
 * (load-balanced).
 */
@Slf4j
@Component
public class DoctorServiceClient {

    private final WebClient webClient;

    public DoctorServiceClient(WebClient.Builder webClientBuilder,
            @Value("${services.doctor-service.url:http://doctor-service}") String doctorServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(doctorServiceUrl).build();
    }

    public SlotReservationResponse reserveSlot(Long slotId) {
        log.info("Attempting to reserve slot: {} in doctor-service", slotId);
        try {
            DoctorServiceApiResponse<SlotReservationResponse> apiResponse = webClient.put()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/doctors/availability/{slotId}/reserve").build(slotId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(),
                            response -> response.createException().flatMap(Mono::error))
                    .onStatus(status -> status.is5xxServerError(),
                            response -> response.createException().flatMap(Mono::error))
                    .bodyToMono(
                            new org.springframework.core.ParameterizedTypeReference<DoctorServiceApiResponse<SlotReservationResponse>>() {
                            })
                    .timeout(Duration.ofSeconds(5))
                    .block();

            return apiResponse != null ? apiResponse.getData() : null;
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw SlotNotAvailableException.forSlot(slotId);
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SlotNotAvailableException("Slot not found: " + slotId);
            }
            throw new DoctorServiceException("Error from doctor-service: " + e.getStatusText(), e);
        } catch (Exception e) {
            throw new DoctorServiceException("Error while calling doctor-service", e);
        }
    }

    public SlotReservationResponse releaseSlot(Long slotId) {
        log.info("Attempting to release slot: {} in doctor-service", slotId);
        try {
            DoctorServiceApiResponse<SlotReservationResponse> apiResponse = webClient.put()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/doctors/availability/{slotId}/release").build(slotId))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(),
                            response -> response.createException().flatMap(Mono::error))
                    .onStatus(status -> status.is5xxServerError(),
                            response -> response.createException().flatMap(Mono::error))
                    .bodyToMono(
                            new org.springframework.core.ParameterizedTypeReference<DoctorServiceApiResponse<SlotReservationResponse>>() {
                            })
                    .timeout(Duration.ofSeconds(5))
                    .block();

            return apiResponse != null ? apiResponse.getData() : null;
        } catch (WebClientResponseException e) {
            log.warn("Failed to release slot {}: {}", slotId, e.getStatusText());
            throw new DoctorServiceException("Failed to release slot: " + slotId, e);
        } catch (Exception e) {
            throw new DoctorServiceException("Error while calling doctor-service", e);
        }
    }
}
