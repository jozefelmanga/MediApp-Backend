package com.mediapp.security_service.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration values controlling JWT issuance.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    @NotBlank
    private String issuer = "https://mediapp.local/security";

    @NotNull
    private Duration accessTokenTtl = Duration.ofMinutes(15);
}
