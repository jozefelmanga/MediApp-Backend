package com.mediapp.doctor_service.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.mediapp.doctor_service.api.dto.CreateAvailabilityRequest;
import com.mediapp.doctor_service.domain.AvailabilitySlotEntity;

/**
 * Unit tests covering the slot generation logic for recurring availability.
 */
class AvailabilitySlotGeneratorTest {

    private final Clock clock = Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
    private final AvailabilitySlotGenerator generator = new AvailabilitySlotGenerator(clock);

    @Test
    void generate_shouldCreateSlotsForRequestedDays() {
        String doctorId = UUID.randomUUID().toString();
        CreateAvailabilityRequest request = CreateAvailabilityRequest.builder()
                .startDate(LocalDate.of(2025, 1, 6))
                .endDate(LocalDate.of(2025, 1, 13))
                .dailyStartTime(LocalTime.of(9, 0))
                .dailyEndTime(LocalTime.of(11, 0))
                .slotDurationMinutes(30)
                .daysOfWeek(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
                .timeZone("UTC")
                .build();

        List<AvailabilitySlotEntity> slots = generator.generate(doctorId, request);

        assertThat(slots).hasSize(12);
        assertThat(slots)
                .allSatisfy(slot -> {
                    assertThat(slot.getDoctorId()).isEqualTo(doctorId);
                    assertThat(slot.isReserved()).isFalse();
                    assertThat(slot.getStartTime()).isBefore(slot.getEndTime());
                    assertThat(slot.getCreatedAt()).isEqualTo(clock.instant());
                });

        assertThat(slots.get(0).getStartTime()).isEqualTo(Instant.parse("2025-01-06T09:00:00Z"));
        assertThat(slots.get(slots.size() - 1).getEndTime()).isEqualTo(Instant.parse("2025-01-13T11:00:00Z"));
    }
}
