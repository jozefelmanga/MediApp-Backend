package com.mediapp.common.auditing;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Externalized settings controlling the shared auditing support.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "mediapp.auditing")
public class AuditingProperties {

    /**
     * Whether to auto-enable JPA auditing when the starter is on the classpath.
     */
    private boolean enabled = true;
}
