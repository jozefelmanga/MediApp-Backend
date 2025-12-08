package com.mediapp.doctor_service.api;

import java.time.OffsetDateTime;
import java.util.List;

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

import com.mediapp.doctor_service.common.dto.ApiResponse;
import com.mediapp.doctor_service.api.dto.AvailabilitySlotResponse;
import com.mediapp.doctor_service.api.dto.CreateAvailabilityRequest;
import com.mediapp.doctor_service.api.dto.CreateDoctorProfileRequest;
import com.mediapp.doctor_service.api.dto.DoctorProfileResponse;
import com.mediapp.doctor_service.api.dto.SlotReservationResponse;
import com.mediapp.doctor_service.service.DoctorAvailabilityService;

import jakarta.validation.Valid;

/**
 * REST endpoints that expose doctor search and availability management
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
        public ResponseEntity<ApiResponse<List<DoctorProfileResponse>>> searchBySpecialty(
                        @RequestParam(value = "specialtyId", required = false) Integer specialtyId) {
                List<DoctorProfileResponse> doctors = doctorAvailabilityService.findDoctorsBySpecialty(specialtyId);
                return ResponseEntity.ok(ApiResponse.success(doctors));
        }

        @GetMapping("/{doctorId}/availability")
        public ResponseEntity<ApiResponse<List<AvailabilitySlotResponse>>> getAvailability(
                        @PathVariable Long doctorId,
                        @RequestParam(value = "from", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) OffsetDateTime from,
                        @RequestParam(value = "to", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) OffsetDateTime to) {
                List<AvailabilitySlotResponse> slots = doctorAvailabilityService.getAvailability(
                                doctorId,
                                from != null ? from.toInstant() : null,
                                to != null ? to.toInstant() : null);
                return ResponseEntity.ok(ApiResponse.success(slots));
        }

        @PostMapping("/{doctorId}/availability")
        public ResponseEntity<ApiResponse<List<AvailabilitySlotResponse>>> createAvailability(
                        @PathVariable Long doctorId,
                        @Valid @RequestBody CreateAvailabilityRequest request) {
                List<AvailabilitySlotResponse> slots = doctorAvailabilityService.createRecurringSlots(doctorId,
                                request);
                return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(slots));
        }

        /**
         * Creates a new doctor profile. Called by user-service when a doctor registers.
         *
         * POST /api/v1/doctors/profiles
         *
         * @param request the doctor profile creation request
         * @return the created doctor profile
         */
        @PostMapping("/profiles")
        public ResponseEntity<ApiResponse<DoctorProfileResponse>> createDoctorProfile(
                        @Valid @RequestBody CreateDoctorProfileRequest request) {
                DoctorProfileResponse profile = doctorAvailabilityService.createDoctorProfile(
                                request.userId(),
                                request.medicalLicenseNumber(),
                                request.specialtyId(),
                                request.officeAddress());
                return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(profile));
        }

        /**
         * Reserve an availability slot (called by booking-service).
         * 
         * PUT /api/v1/doctors/availability/{slotId}/reserve
         *
         * @param slotId  the slot ID to reserve
         * @param request the reservation request containing token
         * @return the reservation response
         */
        @PutMapping("/availability/{slotId}/reserve")
        public ResponseEntity<ApiResponse<SlotReservationResponse>> reserveSlot(
                        @PathVariable Long slotId) {
                SlotReservationResponse reservation = doctorAvailabilityService.reserveSlot(slotId);
                return ResponseEntity.ok(ApiResponse.success(reservation));
        }

        /**
         * Release a previously reserved slot (for cancellation/compensation).
         * 
         * PUT /api/v1/doctors/availability/{slotId}/release
         *
         * @param slotId the slot ID to release
         * @return the slot response after release
         */
        @PutMapping("/availability/{slotId}/release")
        public ResponseEntity<ApiResponse<SlotReservationResponse>> releaseSlot(
                        @PathVariable Long slotId) {
                SlotReservationResponse response = doctorAvailabilityService.releaseSlot(slotId);
                return ResponseEntity.ok(ApiResponse.success(response));
        }
}
