package com.mediapp.user_service.client;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.mediapp.user_service.domain.UserRole;
import com.mediapp.user_service.domain.exception.UserDomainException;
import com.mediapp.user_service.domain.exception.UserErrorCode;

/**
 * REST client for communicating with the security-service.
 * Handles user authentication registration.
 */
@Component
public class SecurityServiceClient {

    private static final Logger log = LoggerFactory.getLogger(SecurityServiceClient.class);

    private final WebClient webClient;

    public SecurityServiceClient(WebClient.Builder webClientBuilder,
            @Value("${services.security-service.url:http://security-service}") String securityServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(securityServiceUrl).build();
    }

    /**
     * Registers a new user in the security-service for authentication.
     *
     * @param email    the user's email
     * @param password the user's raw password
     * @param role     the user's role
     * @return RegisterResponse containing the authUserId
     */
    public RegisterResponse registerUser(String email, String password, UserRole role) {
        RegisterRequest request = new RegisterRequest(email, password, role.name());

        log.info("Registering user in security-service: email={}, role={}", email, role);

        try {
            RegisterResponse response = webClient.post()
                    .uri("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(RegisterResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response == null || response.authUserId() == null) {
                throw new UserDomainException(UserErrorCode.EXTERNAL_SERVICE_ERROR,
                        "Security service returned invalid response");
            }

            log.info("Successfully registered user in security-service: authUserId={}", response.authUserId());
            return response;

        } catch (WebClientResponseException.Conflict e) {
            log.warn("Email already registered in security-service: {}", email);
            throw new UserDomainException(UserErrorCode.EMAIL_ALREADY_USED,
                    "Email already registered: " + email);
        } catch (WebClientResponseException e) {
            log.error("Security service error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new UserDomainException(UserErrorCode.EXTERNAL_SERVICE_ERROR,
                    "Failed to register user in security service: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to communicate with security-service", e);
            throw new UserDomainException(UserErrorCode.EXTERNAL_SERVICE_ERROR,
                    "Security service unavailable: " + e.getMessage());
        }
    }

    /**
     * Looks up an existing user in the security-service by email. Returns null
     * if the user is not found.
     */
    public RegisterResponse lookupUserByEmail(String email) {
        try {
            RegisterResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/auth/lookup").queryParam("email", email).build())
                    .retrieve()
                    .bodyToMono(RegisterResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            return response;
        } catch (WebClientResponseException.NotFound e) {
            return null;
        } catch (WebClientResponseException e) {
            log.error("Security service error during lookup: status={}, body={}", e.getStatusCode(),
                    e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("Failed to lookup user in security-service", e);
            return null;
        }
    }

    /**
     * Request payload for registering a user in security-service.
     */
    public record RegisterRequest(String email, String password, String role) {
    }

    /**
     * Response from security-service registration.
     */
    public record RegisterResponse(Long authUserId, String email, java.util.Set<String> roles) {
    }
}
