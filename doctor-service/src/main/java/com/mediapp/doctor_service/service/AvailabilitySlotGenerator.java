package com.mediapp.doctor_service.service;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.mediapp.doctor_service.api.dto.CreateAvailabilityRequest;
import com.mediapp.doctor_service.domain.AvailabilitySlotEntity;

/**
 * Expands a recurring availability command into concrete slot instances.
 */
@Component
public class AvailabilitySlotGenerator {

    private final Clock clock;

    public AvailabilitySlotGenerator(Clock clock) {
        this.clock = clock;
    }

    public List<AvailabilitySlotEntity> generate(UUID doctorId, CreateAvailabilityRequest request) {
        ZoneId zoneId = resolveZoneId(request.timeZone());
        LocalDate currentDate = request.startDate();
        LocalDate endDate = request.endDate();
        LocalTime dayStart = request.dailyStartTime();
        LocalTime dayEnd = request.dailyEndTime();
        int durationMinutes = request.slotDurationMinutes();
        Set<DayOfWeek> targetDays = request.daysOfWeek();
        Instant now = Instant.now(clock);

        List<AvailabilitySlotEntity> slots = new ArrayList<>();
        while (!currentDate.isAfter(endDate)) {
            if (targetDays.contains(currentDate.getDayOfWeek())) {
                LocalTime cursor = dayStart;
                while (true) {
                    LocalTime slotEnd = cursor.plusMinutes(durationMinutes);
                    if (slotEnd.isAfter(dayEnd)) {
                        break;
                    }

                    ZonedDateTime startDateTime = ZonedDateTime.of(currentDate, cursor, zoneId);
                    ZonedDateTime endDateTime = ZonedDateTime.of(currentDate, slotEnd, zoneId);

                    slots.add(AvailabilitySlotEntity.builder()
                            .id(UUID.randomUUID())
                            .doctorId(doctorId)
                            .startTime(startDateTime.toInstant())
                            .endTime(endDateTime.toInstant())
                            .reserved(false)
                            .reservationToken(null)
                            .reservedAt(null)
                            .version(0L)
                            .createdAt(now)
                            .updatedAt(now)
                            .build());
                    cursor = slotEnd;
                    if (cursor.equals(dayEnd)) {
                        break;
                    }
                }
            }
            currentDate = currentDate.plusDays(1);
        }
        return slots;
    }

    private ZoneId resolveZoneId(String value) {
        try {
            return ZoneId.of(value);
        } catch (DateTimeException ex) {
            throw new IllegalArgumentException("Invalid timezone: " + value, ex);
        }
    }
}
