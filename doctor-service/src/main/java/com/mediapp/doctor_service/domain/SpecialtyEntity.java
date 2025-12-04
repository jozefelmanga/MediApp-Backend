package com.mediapp.doctor_service.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Reactive representation of the specialty catalog table.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("specialty")
public class SpecialtyEntity {

    @Id
    @Column("specialty_id")
    private Integer id;

    @Column("name")
    private String name;
}
