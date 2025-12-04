package com.mediapp.common.auditing;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Auto-configuration that enables JPA auditing with a sensible default auditor
 * provider.
 */
@AutoConfiguration
@EnableConfigurationProperties(AuditingProperties.class)
@ConditionalOnClass(EnableJpaAuditing.class)
@ConditionalOnProperty(prefix = "mediapp.auditing", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableJpaAuditing(auditorAwareRef = "mediappAuditorAware")
public class AuditingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "mediappAuditorAware")
    public AuditorAware<String> mediappAuditorAware() {
        return new SystemAuditorAware();
    }
}
