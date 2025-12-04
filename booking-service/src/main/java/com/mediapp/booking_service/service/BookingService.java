package com.mediapp.booking_service.service;

import com.mediapp.booking_service.dto.*;
import com.mediapp.booking_service.entity.AppointmentStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service interface for booking operations.
 */
public interface BookingService {

    /**
     * Book a new appointment.
     *
     * @param request the booking request
     * @return the appointment confirmation
     */
    AppointmentConfirmation bookAppointment(BookingRequest request);

    /**
     * Cancel an existing appointment.
     *
     * @param appointmentId the appointment ID to cancel
     * @param reason        optional cancellation reason
     * @return the cancellation confirmation
     */
    CancellationConfirmation cancelAppointment(UUID appointmentId, String reason);

    /**
     * Get all appointments for a patient.
     *
     * @param patientId the patient ID
     * @param page      page number (0-indexed)
     * @param size      page size
     * @return paginated list of appointments
     */
    PagedResponse<AppointmentDetail> getPatientAppointments(UUID patientId, int page, int size);

    /**
     * Get appointments for a patient filtered by status.
     *
     * @param patientId the patient ID
     * @param status    the appointment status filter
     * @param page      page number (0-indexed)
     * @param size      page size
     * @return paginated list of appointments
     */
    PagedResponse<AppointmentDetail> getPatientAppointmentsByStatus(
            UUID patientId, AppointmentStatus status, int page, int size);

    /**
     * Get today's appointments for a doctor.
     *
     * @param doctorId the doctor ID
     * @return list of appointments for today
     */
    java.util.List<AppointmentDetail> getDoctorTodayAppointments(UUID doctorId);

    /**
     * Get appointments for a doctor on a specific date.
     *
     * @param doctorId the doctor ID
     * @param date     the date to query
     * @return list of appointments for the date
     */
    java.util.List<AppointmentDetail> getDoctorAppointmentsByDate(UUID doctorId, LocalDate date);

    /**
     * Get a specific appointment by ID.
     *
     * @param appointmentId the appointment ID
     * @return the appointment details
     */
    AppointmentDetail getAppointment(UUID appointmentId);
}
