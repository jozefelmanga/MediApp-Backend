package com.mediapp.security_service.service;

import com.mediapp.security_service.config.properties.AdminBootstrapProperties;
import com.mediapp.security_service.domain.AppUser;
import com.mediapp.security_service.domain.RoleName;
import com.mediapp.security_service.repository.AppUserRepository;
import com.mediapp.security_service.service.dto.RegisterRequest;
import com.mediapp.security_service.service.dto.RegisterResponse;
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

    /**
     * Registers a new user for authentication purposes.
     * This is the single source of truth for user credentials.
     *
     * @param request the registration request containing email, password, and role
     * @return RegisterResponse with the created user's auth ID
     * @throws IllegalArgumentException if email is already registered
     */
    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email already registered: " + normalizedEmail);
        }

        AppUser user = AppUser.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .roles(EnumSet.of(request.role()))
                .enabled(true)
                .build();

        AppUser savedUser = appUserRepository.save(user);
        log.info("Registered new user with email={}, role={}", normalizedEmail, request.role());

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRoles());
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
