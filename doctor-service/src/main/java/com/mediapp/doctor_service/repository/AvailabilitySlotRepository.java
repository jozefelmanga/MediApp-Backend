package com.mediapp.doctor_service.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mediapp.doctor_service.domain.AvailabilitySlotEntity;

/**
 * JPA repository for doctor availability slots.
 */
@Repository
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlotEntity, Long> {

        List<AvailabilitySlotEntity> findByDoctorIdAndStartTimeBetweenOrderByStartTimeAsc(
                        Long doctorId, Instant startTime, Instant endTime);

        @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM AvailabilitySlotEntity s " +
                        "WHERE s.doctorId = :doctorId AND s.startTime < :endTime AND s.endTime > :startTime")
        boolean existsOverlappingSlot(@Param("doctorId") Long doctorId,
                        @Param("startTime") Instant startTime,
                        @Param("endTime") Instant endTime);

        /**
         * Find an unreserved slot by ID for reservation.
         */
        @Query("SELECT s FROM AvailabilitySlotEntity s WHERE s.id = :slotId AND s.reserved = false")
        Optional<AvailabilitySlotEntity> findUnreservedById(@Param("slotId") Long slotId);
}
