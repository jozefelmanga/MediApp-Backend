package com.mediapp.booking_service.controller;

import com.mediapp.booking_service.dto.*;
import com.mediapp.booking_service.entity.AppointmentStatus;
import com.mediapp.booking_service.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for appointment booking operations.
 * Base URL: /api/v1/appointments
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final BookingService bookingService;

    /**
     * Book a new appointment.
     * 
     * POST /api/v1/appointments/book
     *
     * @param request the booking request
     * @return the appointment confirmation
     */
    @PostMapping("/book")
    public ResponseEntity<AppointmentConfirmation> bookAppointment(
            @Valid @RequestBody BookingRequest request) {
        log.info("Received booking request for patient: {} with doctor: {}",
                request.getPatientId(), request.getDoctorId());

        AppointmentConfirmation confirmation = bookingService.bookAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(confirmation);
    }

    /**
     * Cancel an existing appointment.
     * 
     * PUT /api/v1/appointments/cancel/{appointmentId}
     *
     * @param appointmentId the appointment ID to cancel
     * @param reason        optional cancellation reason
     * @return the cancellation confirmation
     */
    @PutMapping("/cancel/{appointmentId}")
    public ResponseEntity<CancellationConfirmation> cancelAppointment(
            @PathVariable Long appointmentId,
            @RequestParam(required = false) String reason) {
        log.info("Received cancellation request for appointment: {}", appointmentId);

        CancellationConfirmation confirmation = bookingService.cancelAppointment(appointmentId, reason);
        return ResponseEntity.ok(confirmation);
    }

    /**
     * Get all appointments for a patient (with optional status filter).
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<PagedResponse<AppointmentDetail>> getPatientAppointments(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) AppointmentStatus status) {
        log.info("Fetching appointments for patient: {}", patientId);

        var response = status != null
                ? bookingService.getPatientAppointmentsByStatus(patientId, status, page, size)
                : bookingService.getPatientAppointments(patientId, page, size);

        return ResponseEntity.ok(response);
    }

    /**
     * Get today's appointments for a doctor.
     * 
     * GET /api/v1/appointments/doctor/{doctorId}/today
     *
     * @param doctorId the doctor ID
     * @return list of today's appointments
     */
    @GetMapping("/doctor/{doctorId}/today")
    public ResponseEntity<List<AppointmentDetail>> getDoctorTodayAppointments(
            @PathVariable Long doctorId) {
        log.info("Fetching today's appointments for doctor: {}", doctorId);

        List<AppointmentDetail> appointments = bookingService.getDoctorTodayAppointments(doctorId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get appointments for a doctor on a specific date.
     * 
     * GET /api/v1/appointments/doctor/{doctorId}/date/{date}
     *
     * @param doctorId the doctor ID
     * @param date     the date to query (format: yyyy-MM-dd)
     * @return list of appointments for the date
     */
    @GetMapping("/doctor/{doctorId}/date/{date}")
    public ResponseEntity<List<AppointmentDetail>> getDoctorAppointmentsByDate(
            @PathVariable Long doctorId,
            @PathVariable LocalDate date) {
        log.info("Fetching appointments for doctor: {} on date: {}", doctorId, date);

        List<AppointmentDetail> appointments = bookingService.getDoctorAppointmentsByDate(doctorId, date);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get a specific appointment by ID.
     * 
     * GET /api/v1/appointments/{appointmentId}
     *
     * @param appointmentId the appointment ID
     * @return the appointment details
     */
    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentDetail> getAppointment(
            @PathVariable Long appointmentId) {
        log.info("Fetching appointment: {}", appointmentId);

        AppointmentDetail appointment = bookingService.getAppointment(appointmentId);
        return ResponseEntity.ok(appointment);
    }
}
