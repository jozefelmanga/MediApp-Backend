package com.mediapp.security_service.service;

import com.mediapp.security_service.config.properties.AdminBootstrapProperties;
import com.mediapp.security_service.domain.AppUser;
import com.mediapp.security_service.domain.RoleName;
import com.mediapp.security_service.repository.AppUserRepository;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Encapsulates basic user persistence operations and bootstrap routines.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmailIgnoreCase(email);
    }

    @Transactional
    public AppUser save(AppUser user) {
        user.setEmail(normalizeEmail(user.getEmail()));
        return appUserRepository.save(user);
    }

    @Transactional
    public boolean ensureAdminUser(AdminBootstrapProperties properties) {
        String normalizedEmail = normalizeEmail(properties.getEmail());
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return false;
        }
        Set<RoleName> roles = properties.getRoles().isEmpty()
                ? EnumSet.of(RoleName.ADMIN)
                : EnumSet.copyOf(properties.getRoles());
        AppUser admin = AppUser.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(properties.getPassword()))
                .roles(roles)
                .build();
        appUserRepository.save(admin);
        log.warn("Seeded default admin user with email={} - update the password immediately.", normalizedEmail);
        return true;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
