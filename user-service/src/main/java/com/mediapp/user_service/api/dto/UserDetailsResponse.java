package com.mediapp.user_service.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mediapp.user_service.common.dto.BaseDto;
import com.mediapp.user_service.domain.UserRole;

/**
 * Detailed view of a user returned by API endpoints.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDetailsResponse(
                Long userId,
                String email,
                String firstName,
                String lastName,
                UserRole role,
                PatientProfileDto patientProfile,
                DoctorProfileDto doctorProfile) implements BaseDto {

        /**
         * Factory method for creating a response with patient profile.
         */
        public static UserDetailsResponse withPatientProfile(Long userId, String email, String firstName,
                        String lastName, UserRole role, PatientProfileDto patientProfile) {
                return new UserDetailsResponse(userId, email, firstName, lastName, role, patientProfile, null);
        }

        /**
         * Factory method for creating a response with doctor profile.
         */
        public static UserDetailsResponse withDoctorProfile(Long userId, String email, String firstName,
                        String lastName, UserRole role, DoctorProfileDto doctorProfile) {
                return new UserDetailsResponse(userId, email, firstName, lastName, role, null, doctorProfile);
        }

        /**
         * Factory method for creating a response without any profile.
         */
        public static UserDetailsResponse withoutProfile(Long userId, String email, String firstName,
                        String lastName, UserRole role) {
                return new UserDetailsResponse(userId, email, firstName, lastName, role, null, null);
        }
}
