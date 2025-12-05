package com.mediapp.user_service.service;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.mediapp.user_service.common.dto.PageMetadata;
import com.mediapp.user_service.common.dto.PageResponse;
import com.mediapp.user_service.api.dto.DoctorRegistrationRequest;
import com.mediapp.user_service.api.dto.PatientProfileDto;
import com.mediapp.user_service.api.dto.PatientRegistrationRequest;
import com.mediapp.user_service.api.dto.PatientSummaryDto;
import com.mediapp.user_service.api.dto.UserDetailsResponse;
import com.mediapp.user_service.client.DoctorServiceClient;
import com.mediapp.user_service.domain.AppUser;
import com.mediapp.user_service.domain.PatientProfile;
import com.mediapp.user_service.domain.UserRole;
import com.mediapp.user_service.domain.exception.UserDomainException;
import com.mediapp.user_service.domain.exception.UserErrorCode;
import com.mediapp.user_service.repository.AppUserRepository;
import com.mediapp.user_service.repository.PatientProfileRepository;

import jakarta.validation.constraints.NotNull;

/**
 * Orchestrates user creation and query workflows.
 */
@Service
@Transactional(readOnly = true)
public class UserManagementService {

    private static final Logger log = LoggerFactory.getLogger(UserManagementService.class);

    private final AppUserRepository appUserRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminTokenValidator adminTokenValidator;
    private final DoctorServiceClient doctorServiceClient;

    public UserManagementService(AppUserRepository appUserRepository,
            PatientProfileRepository patientProfileRepository,
            PasswordEncoder passwordEncoder,
            AdminTokenValidator adminTokenValidator,
            DoctorServiceClient doctorServiceClient) {
        this.appUserRepository = appUserRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminTokenValidator = adminTokenValidator;
        this.doctorServiceClient = doctorServiceClient;
    }

    @Transactional
    public UserDetailsResponse registerPatient(PatientRegistrationRequest request) {
        ensureEmailAvailable(request.email());

        AppUser user = persistUser(request.email(), request.password(), request.firstName(), request.lastName(),
                UserRole.PATIENT);

        PatientProfile profile = PatientProfile.create(user, normalizePhone(request.phoneNumber()),
                request.dateOfBirth());
        PatientProfile persistedProfile = patientProfileRepository.save(profile);
        user.attachPatientProfile(persistedProfile);

        return toUserDetails(user);
    }

    @Transactional
    public UserDetailsResponse registerDoctor(String adminToken, DoctorRegistrationRequest request) {
        adminTokenValidator.validate(adminToken);
        ensureEmailAvailable(request.email());

        AppUser user = persistUser(request.email(), request.password(), request.firstName(), request.lastName(),
                UserRole.DOCTOR);

        // Sync with doctor-service to create the doctor profile
        log.info("Syncing doctor profile with doctor-service for userId: {}", user.getId());
        doctorServiceClient.createDoctorProfileAsync(
                user.getId(),
                request.medicalLicenseNumber(),
                request.specialtyId(),
                request.officeAddress());

        return toUserDetails(user);
    }

    public UserDetailsResponse getUserDetails(Long userId) {
        AppUser user = appUserRepository.findWithPatientProfileById(userId)
                .orElseThrow(() -> new UserDomainException(UserErrorCode.USER_NOT_FOUND,
                        "User not found: " + userId));
        return toUserDetails(user);
    }

    public PageResponse<PatientSummaryDto> listPatients(Pageable pageable) {
        Page<PatientProfile> page = patientProfileRepository.findAllBy(pageable);
        List<PatientSummaryDto> content = page.stream()
                .map(this::toPatientSummary)
                .toList();

        PageMetadata metadata = PageMetadata.builder()
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .build();
        return PageResponse.of(content, metadata);
    }

    private AppUser persistUser(String email, String rawPassword, String firstName, String lastName, UserRole role) {
        AppUser user = AppUser.builder()
                .email(normalizeEmail(email))
                .passwordHash(passwordEncoder.encode(rawPassword))
                .firstName(normalizeName(firstName))
                .lastName(normalizeName(lastName))
                .role(role)
                .build();
        return appUserRepository.save(user);
    }

    private void ensureEmailAvailable(String email) {
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new UserDomainException(UserErrorCode.EMAIL_ALREADY_USED,
                    "An account already exists for email: " + email);
        }
    }

    private String normalizeEmail(@NotNull String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeName(@NotNull String value) {
        return StringUtils.trimWhitespace(value);
    }

    private String normalizePhone(@NotNull String value) {
        return StringUtils.trimWhitespace(value);
    }

    private UserDetailsResponse toUserDetails(AppUser user) {
        PatientProfileDto patientProfileDto = null;
        if (user.getPatientProfile() != null) {
            patientProfileDto = new PatientProfileDto(
                    user.getPatientProfile().getId(),
                    user.getPatientProfile().getPhoneNumber(),
                    user.getPatientProfile().getDateOfBirth());
        }
        return new UserDetailsResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                patientProfileDto);
    }

    private PatientSummaryDto toPatientSummary(PatientProfile profile) {
        AppUser user = profile.getUser();
        return new PatientSummaryDto(
                profile.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                profile.getPhoneNumber(),
                profile.getDateOfBirth());
    }
}
