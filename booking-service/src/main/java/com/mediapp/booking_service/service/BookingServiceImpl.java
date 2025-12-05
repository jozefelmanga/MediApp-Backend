package com.mediapp.booking_service.service;

import com.mediapp.booking_service.client.DoctorServiceClient;
import com.mediapp.booking_service.dto.*;
import com.mediapp.booking_service.entity.Appointment;
import com.mediapp.booking_service.entity.AppointmentStatus;
import com.mediapp.booking_service.event.AppointmentCancelledEvent;
import com.mediapp.booking_service.event.AppointmentCreatedEvent;
import com.mediapp.booking_service.exception.AppointmentCancellationException;
import com.mediapp.booking_service.exception.AppointmentNotFoundException;
import com.mediapp.booking_service.exception.SlotNotAvailableException;
import com.mediapp.booking_service.mapper.AppointmentMapper;
import com.mediapp.booking_service.messaging.AppointmentEventPublisher;
import com.mediapp.booking_service.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Implementation of the BookingService interface.
 * Handles appointment booking, cancellation, and queries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorServiceClient doctorServiceClient;
    private final AppointmentEventPublisher eventPublisher;
    private final AppointmentMapper appointmentMapper;

    // Counter for generating unique event IDs
    private final AtomicLong eventIdCounter = new AtomicLong(System.currentTimeMillis());

    @Override
    @Transactional
    public AppointmentConfirmation bookAppointment(BookingRequest request) {
        log.info("Booking appointment for patient: {} with doctor: {} on slot: {}",
                request.getPatientId(), request.getDoctorId(), request.getSlotId());

        // Check if slot is already booked locally
        if (appointmentRepository.existsBySlotIdAndStatusNotCancelled(request.getSlotId())) {
            log.warn("Slot {} is already booked", request.getSlotId());
            throw SlotNotAvailableException.forSlot(request.getSlotId());
        }

        // Reserve slot in doctor-service
        try {
            doctorServiceClient.reserveSlot(request.getSlotId());
        } catch (SlotNotAvailableException e) {
            log.warn("Slot {} is not available in doctor-service", request.getSlotId());
            throw e;
        }

        // Create and save appointment
        Appointment appointment = Appointment.builder()
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .slotId(request.getSlotId())
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .status(AppointmentStatus.CONFIRMED)
                .build();

        try {
            appointment = appointmentRepository.save(appointment);
            log.info("Appointment created with ID: {}", appointment.getAppointmentId());

            // Publish event to RabbitMQ
            publishAppointmentCreatedEvent(appointment);

            return appointmentMapper.toConfirmation(appointment,
                    "Appointment successfully booked");

        } catch (Exception e) {
            // Compensation: release the slot if appointment persistence fails
            log.error("Failed to save appointment, attempting to release slot: {}", request.getSlotId(), e);
            try {
                doctorServiceClient.releaseSlot(request.getSlotId());
            } catch (Exception releaseEx) {
                log.error("Failed to release slot {} during compensation", request.getSlotId(), releaseEx);
            }
            throw new RuntimeException("Failed to create appointment", e);
        }
    }

    @Override
    @Transactional
    public CancellationConfirmation cancelAppointment(Long appointmentId, String reason) {
        log.info("Cancelling appointment: {}", appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        if (!appointment.cancel()) {
            throw new AppointmentCancellationException(
                    "Cannot cancel appointment with status: " + appointment.getStatus());
        }

        appointment = appointmentRepository.save(appointment);

        // Release slot in doctor-service
        try {
            doctorServiceClient.releaseSlot(appointment.getSlotId());
        } catch (Exception e) {
            log.error("Failed to release slot {} in doctor-service. Manual intervention may be required.",
                    appointment.getSlotId(), e);
            // Continue - appointment is already cancelled locally
        }

        // Publish cancellation event
        publishAppointmentCancelledEvent(appointment, reason);

        return CancellationConfirmation.builder()
                .appointmentId(appointmentId)
                .status(AppointmentStatus.CANCELLED.name())
                .cancelledAt(LocalDateTime.now())
                .message("Appointment successfully cancelled")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AppointmentDetail> getPatientAppointments(Long patientId, int page, int size) {
        log.debug("Fetching appointments for patient: {}, page: {}, size: {}", patientId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointmentPage = appointmentRepository
                .findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patientId, pageable);

        return toPagedResponse(appointmentPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AppointmentDetail> getPatientAppointmentsByStatus(
            Long patientId, AppointmentStatus status, int page, int size) {
        log.debug("Fetching appointments for patient: {} with status: {}", patientId, status);

        Pageable pageable = PageRequest.of(page, size);
        Page<Appointment> appointmentPage = appointmentRepository
                .findByPatientIdAndStatusOrderByAppointmentDateDescStartTimeDesc(patientId, status, pageable);

        return toPagedResponse(appointmentPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDetail> getDoctorTodayAppointments(Long doctorId) {
        log.debug("Fetching today's appointments for doctor: {}", doctorId);

        LocalDate today = LocalDate.now();
        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndAppointmentDateAndStatusOrderByStartTimeAsc(
                        doctorId, today, AppointmentStatus.CONFIRMED);

        return appointments.stream()
                .map(appointmentMapper::toDetail)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDetail> getDoctorAppointmentsByDate(Long doctorId, LocalDate date) {
        log.debug("Fetching appointments for doctor: {} on date: {}", doctorId, date);

        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndAppointmentDateOrderByStartTimeAsc(doctorId, date);

        return appointments.stream()
                .map(appointmentMapper::toDetail)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentDetail getAppointment(Long appointmentId) {
        log.debug("Fetching appointment: {}", appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

        return appointmentMapper.toDetail(appointment);
    }

    private void publishAppointmentCreatedEvent(Appointment appointment) {
        AppointmentCreatedEvent event = AppointmentCreatedEvent.builder()
                .eventId(eventIdCounter.incrementAndGet())
                .appointmentId(appointment.getAppointmentId())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .slotId(appointment.getSlotId())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .createdAt(appointment.getCreatedAt())
                .build();

        try {
            eventPublisher.publishAppointmentCreated(event);
        } catch (Exception e) {
            log.error("Failed to publish AppointmentCreatedEvent for appointment: {}",
                    appointment.getAppointmentId(), e);
            // Don't fail the transaction - event publishing is best-effort
            // Consider implementing outbox pattern for guaranteed delivery
        }
    }

    private void publishAppointmentCancelledEvent(Appointment appointment, String reason) {
        AppointmentCancelledEvent event = AppointmentCancelledEvent.builder()
                .eventId(eventIdCounter.incrementAndGet())
                .appointmentId(appointment.getAppointmentId())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .slotId(appointment.getSlotId())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .cancelledAt(LocalDateTime.now())
                .reason(reason)
                .build();

        try {
            eventPublisher.publishAppointmentCancelled(event);
        } catch (Exception e) {
            log.error("Failed to publish AppointmentCancelledEvent for appointment: {}",
                    appointment.getAppointmentId(), e);
            // Don't fail the transaction - event publishing is best-effort
        }
    }

    private PagedResponse<AppointmentDetail> toPagedResponse(Page<Appointment> page) {
        List<AppointmentDetail> content = page.getContent().stream()
                .map(appointmentMapper::toDetail)
                .collect(Collectors.toList());

        return PagedResponse.<AppointmentDetail>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
