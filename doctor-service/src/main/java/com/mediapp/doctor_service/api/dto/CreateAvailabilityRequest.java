package com.mediapp.doctor_service.api.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

/**
 * Command payload for creating recurring availability slots for a doctor.
 */
@Builder
public record CreateAvailabilityRequest(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull LocalTime dailyStartTime,
        @NotNull LocalTime dailyEndTime,
        @Positive int slotDurationMinutes,
        @NotEmpty Set<DayOfWeek> daysOfWeek,
        @NotBlank String timeZone) {

    @AssertTrue(message = "endDate must be on or after startDate")
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return !endDate.isBefore(startDate);
    }

    @AssertTrue(message = "dailyEndTime must be after dailyStartTime")
    public boolean isValidTimeRange() {
        if (dailyStartTime == null || dailyEndTime == null) {
            return true;
        }
        return dailyEndTime.isAfter(dailyStartTime);
    }
}
