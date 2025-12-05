package com.mediapp.user_service.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Holds patient-specific demographic details linked to an {@link AppUser}.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "patient_profile")
public class PatientProfile {

    @Id
    @Column(name = "patient_id", nullable = false, updatable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "patient_id", referencedColumnName = "user_id")
    private AppUser user;

    @Column(name = "phone_number", length = 20, nullable = false)
    private String phoneNumber;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    public static PatientProfile create(AppUser user, String phoneNumber, LocalDate dateOfBirth) {
        PatientProfile profile = PatientProfile.builder()
                .user(user)
                .phoneNumber(phoneNumber)
                .dateOfBirth(dateOfBirth)
                .build();
        user.attachPatientProfile(profile);
        return profile;
    }
}
