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
import java.util.Set;

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

import com.mediapp.user_service.common.dto.PageResponse;
import com.mediapp.user_service.api.dto.DoctorProfileDto;
import com.mediapp.user_service.api.dto.DoctorRegistrationRequest;
import com.mediapp.user_service.api.dto.PatientRegistrationRequest;
import com.mediapp.user_service.api.dto.PatientSummaryDto;
import com.mediapp.user_service.api.dto.UserDetailsResponse;
import com.mediapp.user_service.client.SecurityServiceClient;
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
        private SecurityServiceClient securityServiceClient;

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
                Long userId = 1L;
                Long authUserId = 100L;
                when(appUserRepository.existsByEmailIgnoreCase(patientRegistrationRequest.email())).thenReturn(false);

                // Mock security service registration
                when(securityServiceClient.registerUser(
                                eq(patientRegistrationRequest.email()),
                                eq(patientRegistrationRequest.password()),
                                eq(UserRole.PATIENT)))
                                .thenReturn(new SecurityServiceClient.RegisterResponse(authUserId,
                                                patientRegistrationRequest.email().toLowerCase(), Set.of("PATIENT")));

                AppUser savedUser = AppUser.builder()
                                .id(userId)
                                .authUserId(authUserId)
                                .email(patientRegistrationRequest.email().toLowerCase())
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

                verify(securityServiceClient).registerUser(any(), any(), any());
                verify(appUserRepository).save(userCaptor.capture());
                verify(patientProfileRepository).save(patientProfileCaptor.capture());

                AppUser persistedUser = userCaptor.getValue();
                assertThat(persistedUser.getRole()).isEqualTo(UserRole.PATIENT);
                assertThat(persistedUser.getEmail()).isEqualTo("user@example.com");
                assertThat(persistedUser.getAuthUserId()).isEqualTo(authUserId);
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
                Long authUserId = 100L;

                when(appUserRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);

                // Mock security service registration
                when(securityServiceClient.registerUser(
                                eq(request.email()),
                                eq(request.password()),
                                eq(UserRole.DOCTOR)))
                                .thenReturn(new SecurityServiceClient.RegisterResponse(authUserId,
                                                request.email().toLowerCase(), Set.of("DOCTOR")));

                AppUser savedUser = AppUser.builder()
                                .id(1L)
                                .authUserId(authUserId)
                                .email(request.email().toLowerCase())
                                .firstName(request.firstName())
                                .lastName(request.lastName())
                                .role(UserRole.DOCTOR)
                                .build();
                when(appUserRepository.save(any(AppUser.class))).thenReturn(savedUser);

                // Mock the doctor-service client to return doctor profile
                DoctorProfileDto mockDoctorProfile = new DoctorProfileDto(
                                1L, "MED-12345", 1, "General Practice", "123 Medical Center");
                when(doctorServiceClient.createDoctorProfileSync(
                                eq(1L), eq("MED-12345"), eq(1), eq("123 Medical Center")))
                                .thenReturn(mockDoctorProfile);

                UserDetailsResponse response = userManagementService.registerDoctor("admin-token", request);

                verify(adminTokenValidator, times(1)).validate(eq("admin-token"));
                verify(securityServiceClient).registerUser(any(), any(), any());
                verify(appUserRepository).save(any(AppUser.class));
                verify(doctorServiceClient).createDoctorProfileSync(eq(1L), eq("MED-12345"), eq(1),
                                eq("123 Medical Center"));
                assertThat(response.role()).isEqualTo(UserRole.DOCTOR);
                assertThat(response.patientProfile()).isNull();
                assertThat(response.doctorProfile()).isNotNull();
                assertThat(response.doctorProfile().medicalLicenseNumber()).isEqualTo("MED-12345");
        }

        @Test
        void getUserDetails_shouldReturnDetails() {
                Long userId = 1L;
                Long authUserId = 100L;
                AppUser user = AppUser.builder()
                                .id(userId)
                                .authUserId(authUserId)
                                .email("patient@example.com")
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
                Long userId = 999L;
                when(appUserRepository.findWithPatientProfileById(userId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> userManagementService.getUserDetails(userId))
                                .isInstanceOf(UserDomainException.class)
                                .extracting(ex -> ((UserDomainException) ex).getErrorCode())
                                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
        }

        @Test
        void listPatients_shouldMapPage() {
                Long userId = 1L;
                Long authUserId = 100L;
                AppUser user = AppUser.builder()
                                .id(userId)
                                .authUserId(authUserId)
                                .email("patient@example.com")
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
