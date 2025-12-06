package com.mediapp.user_service.service;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediapp.user_service.common.dto.PageMetadata;
import com.mediapp.user_service.common.dto.PageResponse;
import com.mediapp.user_service.api.dto.DoctorProfileDto;
import com.mediapp.user_service.api.dto.DoctorRegistrationRequest;
import com.mediapp.user_service.api.dto.PatientProfileDto;
import com.mediapp.user_service.api.dto.PatientRegistrationRequest;
import com.mediapp.user_service.api.dto.PatientSummaryDto;
import com.mediapp.user_service.api.dto.UserDetailsResponse;
import com.mediapp.user_service.client.DoctorServiceClient;
import com.mediapp.user_service.client.SecurityServiceClient;
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
    private final SecurityServiceClient securityServiceClient;
    private final AdminTokenValidator adminTokenValidator;
    private final DoctorServiceClient doctorServiceClient;

    public UserManagementService(AppUserRepository appUserRepository,
            PatientProfileRepository patientProfileRepository,
            SecurityServiceClient securityServiceClient,
            AdminTokenValidator adminTokenValidator,
            DoctorServiceClient doctorServiceClient) {
        this.appUserRepository = appUserRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.securityServiceClient = securityServiceClient;
        this.adminTokenValidator = adminTokenValidator;
        this.doctorServiceClient = doctorServiceClient;
    }

    @Transactional
    public UserDetailsResponse registerPatient(PatientRegistrationRequest request) {
        ensureEmailAvailable(request.email());

        // 1. Register authentication credentials in security-service (single source of
        // truth)
        SecurityServiceClient.RegisterResponse authResponse = securityServiceClient.registerUser(
                request.email(), request.password(), UserRole.PATIENT);

        // 2. Create user profile in user-service (no password stored here)
        AppUser user = persistUser(
                authResponse.authUserId(),
                request.email(),
                request.firstName(),
                request.lastName(),
                UserRole.PATIENT);

        // 3. Create patient profile
        PatientProfile profile = PatientProfile.create(user, normalizePhone(request.phoneNumber()),
                request.dateOfBirth());
        PatientProfile persistedProfile = patientProfileRepository.save(profile);
        user.attachPatientProfile(persistedProfile);

        log.info("Patient registered successfully: userId={}, authUserId={}", user.getId(), authResponse.authUserId());
        return toUserDetails(user);
    }

    @Transactional
    public UserDetailsResponse registerDoctor(String adminToken, DoctorRegistrationRequest request) {
        adminTokenValidator.validate(adminToken);
        ensureEmailAvailable(request.email());

        // 1. Register authentication credentials in security-service
        SecurityServiceClient.RegisterResponse authResponse = securityServiceClient.registerUser(
                request.email(), request.password(), UserRole.DOCTOR);

        // 2. Create user profile in user-service
        AppUser user = persistUser(
                authResponse.authUserId(),
                request.email(),
                request.firstName(),
                request.lastName(),
                UserRole.DOCTOR);

        // 3. Sync with doctor-service to create the doctor profile
        log.info("Creating doctor profile in doctor-service for userId: {}", user.getId());
        DoctorProfileDto doctorProfile = doctorServiceClient.createDoctorProfileSync(
                user.getId(),
                request.medicalLicenseNumber(),
                request.specialtyId(),
                request.officeAddress());

        log.info("Doctor registered successfully: userId={}, authUserId={}", user.getId(), authResponse.authUserId());
        return toUserDetailsWithDoctorProfile(user, doctorProfile);
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

    private AppUser persistUser(Long authUserId, String email, String firstName, String lastName, UserRole role) {
        AppUser user = AppUser.builder()
                .authUserId(authUserId)
                .email(normalizeEmail(email))
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
        return value.strip();
    }

    private String normalizePhone(@NotNull String value) {
        return value.strip();
    }

    private UserDetailsResponse toUserDetails(AppUser user) {
        PatientProfileDto patientProfileDto = null;
        if (user.getPatientProfile() != null) {
            patientProfileDto = new PatientProfileDto(
                    user.getPatientProfile().getId(),
                    user.getPatientProfile().getPhoneNumber(),
                    user.getPatientProfile().getDateOfBirth());
        }
        return UserDetailsResponse.withPatientProfile(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                patientProfileDto);
    }

    private UserDetailsResponse toUserDetailsWithDoctorProfile(AppUser user, DoctorProfileDto doctorProfile) {
        return UserDetailsResponse.withDoctorProfile(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                doctorProfile);
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
