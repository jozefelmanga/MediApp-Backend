package com.mediapp.doctor_service.domain;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Reactive representation of the availability_slot table.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("availability_slot")
public class AvailabilitySlotEntity {

    @Id
    @Column("slot_id")
    private UUID id;

    @Column("doctor_id")
    private UUID doctorId;

    @Column("start_time")
    private Instant startTime;

    @Column("end_time")
    private Instant endTime;

    @Column("is_reserved")
    private boolean reserved;

    @Column("reservation_token")
    private String reservationToken;

    @Column("reserved_at")
    private Instant reservedAt;

    @Version
    @Column("version")
    private Long version;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    public AvailabilitySlotEntity markReserved(String token, Instant reservedAtInstant) {
        return AvailabilitySlotEntity.builder()
                .id(id)
                .doctorId(doctorId)
                .startTime(startTime)
                .endTime(endTime)
                .reserved(true)
                .reservationToken(token)
                .reservedAt(reservedAtInstant)
                .version(version)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
