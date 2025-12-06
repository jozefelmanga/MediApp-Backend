package com.mediapp.gateway_service.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the primary route map for MediApp downstream services.
 * Routes forward the full path (including /api/v1) to downstream services.
 */
@Configuration
public class GatewayRoutesConfig {

        private static final String API_PREFIX = "/api/v1";

        @Bean
        RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
                return builder.routes()
                                .route("security-service", route -> route
                                                .path(API_PREFIX + "/auth/**")
                                                .uri("lb://security-service"))
                                .route("user-service", route -> route
                                                .path(API_PREFIX + "/users/**")
                                                .uri("lb://user-service"))
                                .route("doctor-service", route -> route
                                                .path(API_PREFIX + "/doctors/**")
                                                .uri("lb://doctor-service"))
                                .route("booking-service", route -> route
                                                .path(API_PREFIX + "/bookings/**")
                                                .uri("lb://booking-service"))
                                .route("notification-service", route -> route
                                                .path(API_PREFIX + "/notifications/**")
                                                .uri("lb://notification-service"))
                                .route("catalogue-service", route -> route
                                                .path(API_PREFIX + "/catalogue/**")
                                                .uri("lb://catalogue-service"))
                                .build();
        }
}
