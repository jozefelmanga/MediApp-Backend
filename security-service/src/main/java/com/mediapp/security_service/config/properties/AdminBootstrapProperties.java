package com.mediapp.security_service.config.properties;

import com.mediapp.security_service.domain.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Settings used when seeding the initial administrative user.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "security.bootstrap.admin")
public class AdminBootstrapProperties {

    @Email
    private String email = "admin@mediapp.local";

    @NotBlank
    private String password = "ChangeMe123!";

    private Set<RoleName> roles = EnumSet.of(RoleName.ADMIN);
}
