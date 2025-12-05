package com.mediapp.user_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * REST client for communicating with the doctor-service.
 */
@Component
public class DoctorServiceClient {

        private static final Logger log = LoggerFactory.getLogger(DoctorServiceClient.class);

        private final WebClient webClient;
        private final String doctorServiceUrl;

        public DoctorServiceClient(WebClient.Builder webClientBuilder,
                        @Value("${services.doctor-service.url:http://localhost:8082}") String doctorServiceUrl) {
                this.webClient = webClientBuilder.baseUrl(doctorServiceUrl).build();
                this.doctorServiceUrl = doctorServiceUrl;
        }

        /**
         * Creates a doctor profile in the doctor-service.
         *
         * @param doctorId             the ID of the doctor (same as user ID)
         * @param medicalLicenseNumber the doctor's medical license number
         * @param specialtyId          the specialty ID
         * @param officeAddress        the doctor's office address
         * @return Mono<Void> that completes when the profile is created
         */
        public Mono<Void> createDoctorProfile(Long doctorId, String medicalLicenseNumber,
                        Integer specialtyId, String officeAddress) {

                CreateDoctorProfileRequest request = new CreateDoctorProfileRequest(
                                doctorId, medicalLicenseNumber, specialtyId, officeAddress);

                log.info("Creating doctor profile in doctor-service for doctorId: {}", doctorId);

                return webClient.post()
                                .uri("/api/v1/doctors/profiles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(request)
                                .retrieve()
                                .toBodilessEntity()
                                .doOnSuccess(response -> log
                                                .info("Successfully created doctor profile for doctorId: {}", doctorId))
                                .doOnError(error -> log.error(
                                                "Failed to create doctor profile for doctorId: {}. Error: {}",
                                                doctorId, error.getMessage()))
                                .then();
        }

        /**
         * Creates a doctor profile asynchronously (fire-and-forget with retry).
         * This is useful when you want to continue without waiting for the response.
         */
        public void createDoctorProfileAsync(Long doctorId, String medicalLicenseNumber,
                        Integer specialtyId, String officeAddress) {

                createDoctorProfile(doctorId, medicalLicenseNumber, specialtyId, officeAddress)
                                .retry(3)
                                .subscribe(
                                                null,
                                                error -> log.error(
                                                                "Failed to create doctor profile after retries for doctorId: {}. Error: {}",
                                                                doctorId, error.getMessage()),
                                                () -> log.debug("Doctor profile creation completed for doctorId: {}",
                                                                doctorId));
        }
}
