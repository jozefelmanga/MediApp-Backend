package com.mediapp.doctor_service.api;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mediapp.common.dto.ApiResponse;
import com.mediapp.doctor_service.api.dto.AvailabilitySlotResponse;
import com.mediapp.doctor_service.api.dto.CreateAvailabilityRequest;
import com.mediapp.doctor_service.api.dto.DoctorProfileResponse;
import com.mediapp.doctor_service.api.dto.ReserveSlotRequest;
import com.mediapp.doctor_service.api.dto.SlotReservationResponse;
import com.mediapp.doctor_service.service.DoctorAvailabilityService;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

/**
 * Reactive REST endpoints that expose doctor search and availability management
 * workflows.
 */
@RestController
@RequestMapping("/api/v1/doctors")
@Validated
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService doctorAvailabilityService;

    public DoctorAvailabilityController(DoctorAvailabilityService doctorAvailabilityService) {
        this.doctorAvailabilityService = doctorAvailabilityService;
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<DoctorProfileResponse>>>> searchBySpecialty(
            @RequestParam("specialtyId") Integer specialtyId) {
        return doctorAvailabilityService.findDoctorsBySpecialty(specialtyId)
                .map(ApiResponse::success)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{doctorId}/availability")
    public Mono<ResponseEntity<ApiResponse<List<AvailabilitySlotResponse>>>> getAvailability(
            @PathVariable UUID doctorId,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) OffsetDateTime to) {
        return doctorAvailabilityService.getAvailability(doctorId,
                from != null ? from.toInstant() : null,
                to != null ? to.toInstant() : null)
                .map(ApiResponse::success)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{doctorId}/availability")
    public Mono<ResponseEntity<ApiResponse<List<AvailabilitySlotResponse>>>> createAvailability(
            @PathVariable UUID doctorId,
            @Valid @RequestBody CreateAvailabilityRequest request) {
        return doctorAvailabilityService.createRecurringSlots(doctorId, request)
                .map(ApiResponse::success)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @PutMapping("/internal/availability/{slotId}/reserve")
    public Mono<ResponseEntity<ApiResponse<SlotReservationResponse>>> reserveSlot(
            @PathVariable UUID slotId,
            @Valid @RequestBody ReserveSlotRequest request) {
        return doctorAvailabilityService.reserveSlot(slotId, request.reservationToken())
                .map(ApiResponse::success)
                .map(ResponseEntity::ok);
    }
}
