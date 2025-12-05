package com.mediapp.doctor_service.domain;

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
 * JPA entity representing the specialty catalog table.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "specialty")
public class SpecialtyEntity {

    @Id
    @Column(name = "specialty_id")
    private Integer id;

    @Column(name = "name")
    private String name;
}
