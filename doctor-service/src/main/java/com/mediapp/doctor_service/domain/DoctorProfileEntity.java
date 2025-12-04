package com.mediapp.doctor_service.domain;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Reactive representation of the doctor_profile table.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("doctor_profile")
public class DoctorProfileEntity implements Persistable<String> {

    @Id
    @Column("doctor_id")
    private String id;

    @Column("medical_license_number")
    private String medicalLicenseNumber;

    @Column("specialty_id")
    private Integer specialtyId;

    @Column("office_address")
    private String officeAddress;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }
}
