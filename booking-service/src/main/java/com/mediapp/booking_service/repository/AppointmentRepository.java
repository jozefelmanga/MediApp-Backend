package com.mediapp.booking_service.repository;

import com.mediapp.booking_service.entity.Appointment;
import com.mediapp.booking_service.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Appointment entities.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

        /**
         * Find all appointments for a specific patient.
         *
         * @param patientId the patient's ID
         * @param pageable  pagination information
         * @return page of appointments
         */
        Page<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(Long patientId, Pageable pageable);

        /**
         * Find all appointments for a patient with a specific status.
         *
         * @param patientId the patient's ID
         * @param status    the appointment status
         * @param pageable  pagination information
         * @return page of appointments
         */
        Page<Appointment> findByPatientIdAndStatusOrderByAppointmentDateDescStartTimeDesc(
                        Long patientId, AppointmentStatus status, Pageable pageable);

        /**
         * Find all appointments for a doctor on a specific date.
         *
         * @param doctorId        the doctor's ID
         * @param appointmentDate the date to query
         * @return list of appointments
         */
        List<Appointment> findByDoctorIdAndAppointmentDateOrderByStartTimeAsc(Long doctorId, LocalDate appointmentDate);

        /**
         * Find confirmed appointments for a doctor on a specific date.
         *
         * @param doctorId        the doctor's ID
         * @param appointmentDate the date to query
         * @param status          the appointment status
         * @return list of appointments
         */
        List<Appointment> findByDoctorIdAndAppointmentDateAndStatusOrderByStartTimeAsc(
                        Long doctorId, LocalDate appointmentDate, AppointmentStatus status);

        /**
         * Find all appointments for a doctor with pagination.
         *
         * @param doctorId the doctor's ID
         * @param pageable pagination information
         * @return page of appointments
         */
        Page<Appointment> findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(Long doctorId, Pageable pageable);

        /**
         * Check if a slot is already booked (not cancelled).
         *
         * @param slotId the slot ID
         * @return true if the slot is booked
         */
        @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.slotId = :slotId AND a.status != 'CANCELLED'")
        boolean existsBySlotIdAndStatusNotCancelled(@Param("slotId") Long slotId);

        /**
         * Find appointment by slot ID.
         *
         * @param slotId the slot ID
         * @return optional appointment
         */
        Optional<Appointment> findBySlotId(Long slotId);

        /**
         * Find upcoming appointments for a patient (from today onwards).
         *
         * @param patientId the patient's ID
         * @param fromDate  the start date
         * @param status    the appointment status
         * @param pageable  pagination information
         * @return page of appointments
         */
        @Query("SELECT a FROM Appointment a WHERE a.patientId = :patientId " +
                        "AND a.appointmentDate >= :fromDate AND a.status = :status " +
                        "ORDER BY a.appointmentDate ASC, a.startTime ASC")
        Page<Appointment> findUpcomingAppointments(
                        @Param("patientId") Long patientId,
                        @Param("fromDate") LocalDate fromDate,
                        @Param("status") AppointmentStatus status,
                        Pageable pageable);

        /**
         * Count appointments by status for a patient.
         *
         * @param patientId the patient's ID
         * @param status    the appointment status
         * @return count of appointments
         */
        long countByPatientIdAndStatus(Long patientId, AppointmentStatus status);
}
