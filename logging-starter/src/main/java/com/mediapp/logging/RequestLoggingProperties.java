package com.mediapp.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Configurable properties for MediApp logging starter.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "mediapp.logging")
public class RequestLoggingProperties {

    /** Header name that carries the correlation identifier. */
    private String correlationHeader = "X-Correlation-Id";

    /** Whether to log inbound requests at INFO level. */
    private boolean logInboundRequests = true;
}
