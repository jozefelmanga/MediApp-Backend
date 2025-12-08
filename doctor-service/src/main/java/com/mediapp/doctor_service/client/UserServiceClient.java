package com.mediapp.doctor_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;

/**
 * Client to call user-service for fetching user details.
 */
@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder webClientBuilder) {
        // use service id for load-balanced requests
        this.webClient = webClientBuilder.baseUrl("http://user-service").build();
    }

    public UserDetailsDto getUserDetails(Long userId) {
        try {
            RemoteApiResponse<UserDetailsDto> resp = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/users/details/{id}").build(userId))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<RemoteApiResponse<UserDetailsDto>>() {
                    })
                    .block();

            if (resp != null && resp.success()) {
                return resp.data();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch user details for userId={} : {}", userId, e.getMessage());
        }
        return null;
    }
}
