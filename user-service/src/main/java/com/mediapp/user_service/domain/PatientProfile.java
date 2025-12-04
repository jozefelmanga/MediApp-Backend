package com.mediapp.user_service.domain;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "patient_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;

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
