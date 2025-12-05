package com.mediapp.doctor_service.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing the doctor_profile table.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "doctor_profile")
public class DoctorProfileEntity {

    @Id
    @Column(name = "doctor_id")
    private String id;

    @Column(name = "medical_license_number")
    private String medicalLicenseNumber;

    @Column(name = "specialty_id")
    private Integer specialtyId;

    @Column(name = "office_address")
    private String officeAddress;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
