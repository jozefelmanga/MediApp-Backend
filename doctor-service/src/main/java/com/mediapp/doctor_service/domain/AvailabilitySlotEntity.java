package com.mediapp.doctor_service.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing the availability_slot table.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "availability_slot")
public class AvailabilitySlotEntity {

    @Id
    @Column(name = "slot_id")
    private String id;

    @Column(name = "doctor_id")
    private String doctorId;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "is_reserved")
    private boolean reserved;

    @Column(name = "reservation_token")
    private String reservationToken;

    @Column(name = "reserved_at")
    private Instant reservedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public void markReserved(String token, Instant reservedAtInstant) {
        this.reserved = true;
        this.reservationToken = token;
        this.reservedAt = reservedAtInstant;
        this.updatedAt = reservedAtInstant;
    }
}
