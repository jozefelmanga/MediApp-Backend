package com.mediapp.user_service.client;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.mediapp.user_service.api.dto.DoctorProfileDto;

import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * REST client for communicating with the doctor-service.
 */
@Component
public class DoctorServiceClient {

        private static final Logger log = LoggerFactory.getLogger(DoctorServiceClient.class);

        private final WebClient webClient;

        public DoctorServiceClient(WebClient.Builder webClientBuilder,
                        @Value("${services.doctor-service.url:http://doctor-service}") String doctorServiceUrl) {
                this.webClient = webClientBuilder.baseUrl(doctorServiceUrl).build();
        }

        /**
         * Creates a doctor profile in the doctor-service synchronously.
         *
         * @param doctorId             the ID of the doctor (same as user ID)
         * @param medicalLicenseNumber the doctor's medical license number
         * @param specialtyId          the specialty ID
         * @param officeAddress        the doctor's office address
         * @return DoctorProfileDto the created doctor profile
         */
        public DoctorProfileDto createDoctorProfileSync(Long doctorId, String medicalLicenseNumber,
                        Integer specialtyId, String officeAddress) {

                CreateDoctorProfileRequest request = new CreateDoctorProfileRequest(
                                doctorId, medicalLicenseNumber, specialtyId, officeAddress);

                log.info("Creating doctor profile in doctor-service for doctorId: {}", doctorId);

                try {
                        DoctorServiceApiResponse response = webClient.post()
                                        .uri("/api/v1/doctors/profiles")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(request)
                                        .retrieve()
                                        .bodyToMono(new ParameterizedTypeReference<DoctorServiceApiResponse>() {
                                        })
                                        .timeout(Duration.ofSeconds(10))
                                        .block();

                        if (response != null && response.data() != null) {
                                log.info("Successfully created doctor profile for doctorId: {}", doctorId);
                                DoctorServiceProfileData data = response.data();
                                return new DoctorProfileDto(
                                                data.doctorId(),
                                                data.medicalLicenseNumber(),
                                                data.specialtyId(),
                                                data.specialtyName(),
                                                data.officeAddress());
                        }
                        log.warn("Empty response received from doctor-service for doctorId: {}", doctorId);
                        return null;
                } catch (WebClientResponseException e) {
                        log.error("Doctor-service returned error while creating profile: status={}, body={}",
                                        e.getStatusCode(), e.getResponseBodyAsString());
                        throw new RuntimeException(
                                        "Failed to create doctor profile in doctor-service: " + e.getStatusCode()
                                                        + " - "
                                                        + e.getResponseBodyAsString(),
                                        e);
                } catch (Exception e) {
                        log.error("Failed to create doctor profile for doctorId: {}. Error: {}", doctorId,
                                        e.getMessage());
                        throw new RuntimeException("Failed to create doctor profile in doctor-service", e);
                }
        }

        /**
         * Creates a doctor profile asynchronously (fire-and-forget with retry).
         * This is useful when you want to continue without waiting for the response.
         */
        public void createDoctorProfileAsync(Long doctorId, String medicalLicenseNumber,
                        Integer specialtyId, String officeAddress) {

                CreateDoctorProfileRequest request = new CreateDoctorProfileRequest(
                                doctorId, medicalLicenseNumber, specialtyId, officeAddress);

                webClient.post()
                                .uri("/api/v1/doctors/profiles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(request)
                                .retrieve()
                                .toBodilessEntity()
                                .retry(3)
                                .subscribe(
                                                response -> log.info(
                                                                "Successfully created doctor profile for doctorId: {}",
                                                                doctorId),
                                                error -> log.error(
                                                                "Failed to create doctor profile after retries for doctorId: {}. Error: {}",
                                                                doctorId, error.getMessage()));
        }

        /**
         * Response wrapper from doctor-service API.
         */
        private record DoctorServiceApiResponse(
                        boolean success,
                        DoctorServiceProfileData data,
                        String message) {
        }

        /**
         * Doctor profile data from doctor-service response.
         */
        private record DoctorServiceProfileData(
                        Long doctorId,
                        Long userId,
                        String medicalLicenseNumber,
                        Integer specialtyId,
                        String specialtyName,
                        String officeAddress) {
        }
}
