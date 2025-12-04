package com.mediapp.booking_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration for HTTP clients used by the booking service.
 */
@Configuration
public class RestClientConfig {

    /**
     * Creates a RestClient.Builder bean for dependency injection.
     *
     * @return a new RestClient.Builder instance
     */
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
