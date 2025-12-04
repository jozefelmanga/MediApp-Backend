package com.mediapp.common.auditing;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;

/**
 * Default auditor when services do not provide request-based user details yet.
 */
public class SystemAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of("system");
    }
}
