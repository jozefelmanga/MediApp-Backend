package com.mediapp.user_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mediapp.user_service.common.dto.PageResponse;
import com.mediapp.user_service.api.dto.DoctorRegistrationRequest;
import com.mediapp.user_service.api.dto.PatientRegistrationRequest;
import com.mediapp.user_service.api.dto.PatientSummaryDto;
import com.mediapp.user_service.api.dto.UserDetailsResponse;
import com.mediapp.user_service.domain.AppUser;
import com.mediapp.user_service.domain.PatientProfile;
import com.mediapp.user_service.domain.UserRole;
import com.mediapp.user_service.domain.exception.UserDomainException;
import com.mediapp.user_service.domain.exception.UserErrorCode;
import com.mediapp.user_service.repository.AppUserRepository;
import com.mediapp.user_service.repository.PatientProfileRepository;
import com.mediapp.user_service.client.DoctorServiceClient;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

        @Mock
        private AppUserRepository appUserRepository;

        @Mock
        private PatientProfileRepository patientProfileRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private AdminTokenValidator adminTokenValidator;

        @Mock
        private DoctorServiceClient doctorServiceClient;

        @InjectMocks
        private UserManagementService userManagementService;

        @Captor
        private ArgumentCaptor<AppUser> userCaptor;

        @Captor
        private ArgumentCaptor<PatientProfile> patientProfileCaptor;

        private PatientRegistrationRequest patientRegistrationRequest;

        @BeforeEach
        void setUp() {
                patientRegistrationRequest = new PatientRegistrationRequest(
                                "user@example.com",
                                "Password9",
                                "Jane",
                                "Doe",
                                "5551234567",
                                LocalDate.of(1995, 5, 12));
        }

        @Test
        void registerPatient_shouldPersistUserAndProfile() {
                UUID userId = UUID.randomUUID();
                when(appUserRepository.existsByEmailIgnoreCase(patientRegistrationRequest.email())).thenReturn(false);
                when(passwordEncoder.encode(patientRegistrationRequest.password())).thenReturn("encoded-pass");

                AppUser savedUser = AppUser.builder()
                                .id(userId)
                                .email(patientRegistrationRequest.email().toLowerCase())
                                .passwordHash("encoded-pass")
                                .firstName(patientRegistrationRequest.firstName())
                                .lastName(patientRegistrationRequest.lastName())
                                .role(UserRole.PATIENT)
                                .build();

                when(appUserRepository.save(any(AppUser.class))).thenReturn(savedUser);

                PatientProfile savedProfile = PatientProfile.builder()
                                .id(userId)
                                .user(savedUser)
                                .phoneNumber(patientRegistrationRequest.phoneNumber())
                                .dateOfBirth(patientRegistrationRequest.dateOfBirth())
                                .build();
                when(patientProfileRepository.save(any(PatientProfile.class))).thenReturn(savedProfile);

                UserDetailsResponse response = userManagementService.registerPatient(patientRegistrationRequest);

                verify(appUserRepository).save(userCaptor.capture());
                verify(patientProfileRepository).save(patientProfileCaptor.capture());

                AppUser persistedUser = userCaptor.getValue();
                assertThat(persistedUser.getRole()).isEqualTo(UserRole.PATIENT);
                assertThat(persistedUser.getEmail()).isEqualTo("user@example.com");
                assertThat(response.patientProfile()).isNotNull();
                assertThat(response.patientProfile().patientId()).isEqualTo(userId);
                assertThat(patientProfileCaptor.getValue().getUser()).isEqualTo(savedUser);
        }

        @Test
        void registerPatient_shouldRejectDuplicateEmail() {
                when(appUserRepository.existsByEmailIgnoreCase(patientRegistrationRequest.email())).thenReturn(true);

                assertThatThrownBy(() -> userManagementService.registerPatient(patientRegistrationRequest))
                                .isInstanceOf(UserDomainException.class)
                                .hasMessageContaining("already exists")
                                .extracting(ex -> ((UserDomainException) ex).getErrorCode())
                                .isEqualTo(UserErrorCode.EMAIL_ALREADY_USED);
        }

        @Test
        void registerDoctor_shouldValidateAdminTokenAndPersistUser() {
                DoctorRegistrationRequest request = new DoctorRegistrationRequest(
                                "doc@example.com", "Password9", "Doc", "Tor",
                                "MED-12345", 1, "123 Medical Center");
                when(appUserRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
                when(passwordEncoder.encode(request.password())).thenReturn("encoded-pass");

                AppUser savedUser = AppUser.builder()
                                .id(UUID.randomUUID())
                                .email(request.email().toLowerCase())
                                .passwordHash("encoded-pass")
                                .firstName(request.firstName())
                                .lastName(request.lastName())
                                .role(UserRole.DOCTOR)
                                .build();
                when(appUserRepository.save(any(AppUser.class))).thenReturn(savedUser);

                UserDetailsResponse response = userManagementService.registerDoctor("admin-token", request);

                verify(adminTokenValidator, times(1)).validate(eq("admin-token"));
                verify(appUserRepository).save(any(AppUser.class));
                assertThat(response.role()).isEqualTo(UserRole.DOCTOR);
                assertThat(response.patientProfile()).isNull();
        }

        @Test
        void getUserDetails_shouldReturnDetails() {
                UUID userId = UUID.randomUUID();
                AppUser user = AppUser.builder()
                                .id(userId)
                                .email("patient@example.com")
                                .passwordHash("hash")
                                .firstName("Pat")
                                .lastName("Ient")
                                .role(UserRole.PATIENT)
                                .build();
                PatientProfile profile = PatientProfile.builder()
                                .id(userId)
                                .user(user)
                                .phoneNumber("5551234567")
                                .dateOfBirth(LocalDate.of(1990, 1, 1))
                                .build();
                user.attachPatientProfile(profile);

                when(appUserRepository.findWithPatientProfileById(userId)).thenReturn(Optional.of(user));

                UserDetailsResponse response = userManagementService.getUserDetails(userId);

                assertThat(response.userId()).isEqualTo(userId);
                assertThat(response.patientProfile()).isNotNull();
                assertThat(response.patientProfile().phoneNumber()).isEqualTo("5551234567");
        }

        @Test
        void getUserDetails_shouldThrowWhenMissing() {
                UUID userId = UUID.randomUUID();
                when(appUserRepository.findWithPatientProfileById(userId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> userManagementService.getUserDetails(userId))
                                .isInstanceOf(UserDomainException.class)
                                .extracting(ex -> ((UserDomainException) ex).getErrorCode())
                                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
        }

        @Test
        void listPatients_shouldMapPage() {
                UUID userId = UUID.randomUUID();
                AppUser user = AppUser.builder()
                                .id(userId)
                                .email("patient@example.com")
                                .passwordHash("hash")
                                .firstName("Pat")
                                .lastName("Ient")
                                .role(UserRole.PATIENT)
                                .build();
                PatientProfile profile = PatientProfile.builder()
                                .id(userId)
                                .user(user)
                                .phoneNumber("5551234567")
                                .dateOfBirth(LocalDate.of(1990, 1, 1))
                                .build();
                user.attachPatientProfile(profile);

                Page<PatientProfile> page = new PageImpl<>(List.of(profile), PageRequest.of(0, 20), 1);
                when(patientProfileRepository.findAllBy(any(Pageable.class))).thenReturn(page);

                PageResponse<PatientSummaryDto> response = userManagementService.listPatients(PageRequest.of(0, 20));

                assertThat(response.content()).hasSize(1);
                assertThat(response.content().get(0).email()).isEqualTo("patient@example.com");
                assertThat(response.page().totalElements()).isEqualTo(1);
        }
}
