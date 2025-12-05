package com.mediapp.booking_service.service;

import com.mediapp.booking_service.client.DoctorServiceClient;
import com.mediapp.booking_service.client.SlotReservationResponse;
import com.mediapp.booking_service.dto.*;
import com.mediapp.booking_service.entity.Appointment;
import com.mediapp.booking_service.entity.AppointmentStatus;
import com.mediapp.booking_service.exception.AppointmentCancellationException;
import com.mediapp.booking_service.exception.AppointmentNotFoundException;
import com.mediapp.booking_service.exception.SlotNotAvailableException;
import com.mediapp.booking_service.mapper.AppointmentMapper;
import com.mediapp.booking_service.messaging.AppointmentEventPublisher;
import com.mediapp.booking_service.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorServiceClient doctorServiceClient;

    @Mock
    private AppointmentEventPublisher eventPublisher;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Long patientId;
    private Long doctorId;
    private Long slotId;
    private Long appointmentId;
    private BookingRequest bookingRequest;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        patientId = 1L;
        doctorId = 2L;
        slotId = 123L;
        appointmentId = 100L;

        bookingRequest = BookingRequest.builder()
                .patientId(patientId)
                .doctorId(doctorId)
                .slotId(slotId)
                .appointmentDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .build();

        appointment = Appointment.builder()
                .appointmentId(appointmentId)
                .patientId(patientId)
                .doctorId(doctorId)
                .slotId(slotId)
                .appointmentDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .status(AppointmentStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Book Appointment Tests")
    class BookAppointmentTests {

        @Test
        @DisplayName("Should successfully book an appointment")
        void shouldBookAppointmentSuccessfully() {
            // Given
            when(appointmentRepository.existsBySlotIdAndStatusNotCancelled(slotId)).thenReturn(false);
            when(doctorServiceClient.reserveSlot(slotId)).thenReturn(
                    SlotReservationResponse.builder()
                            .slotId(slotId)
                            .build());
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
            when(appointmentMapper.toConfirmation(any(), any())).thenReturn(
                    AppointmentConfirmation.builder()
                            .appointmentId(appointmentId)
                            .status(AppointmentStatus.CONFIRMED)
                            .message("Appointment successfully booked")
                            .build());

            // When
            AppointmentConfirmation result = bookingService.bookAppointment(bookingRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAppointmentId()).isEqualTo(appointmentId);
            assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);

            verify(appointmentRepository).existsBySlotIdAndStatusNotCancelled(slotId);
            verify(doctorServiceClient).reserveSlot(slotId);
            verify(appointmentRepository).save(any(Appointment.class));
            verify(eventPublisher).publishAppointmentCreated(any());
        }

        @Test
        @DisplayName("Should throw exception when slot is already booked locally")
        void shouldThrowExceptionWhenSlotAlreadyBookedLocally() {
            // Given
            when(appointmentRepository.existsBySlotIdAndStatusNotCancelled(slotId)).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> bookingService.bookAppointment(bookingRequest))
                    .isInstanceOf(SlotNotAvailableException.class);

            verify(doctorServiceClient, never()).reserveSlot(any());
            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when slot is not available in doctor service")
        void shouldThrowExceptionWhenSlotNotAvailableInDoctorService() {
            // Given
            when(appointmentRepository.existsBySlotIdAndStatusNotCancelled(slotId)).thenReturn(false);
            when(doctorServiceClient.reserveSlot(slotId)).thenThrow(SlotNotAvailableException.forSlot(slotId));

            // When/Then
            assertThatThrownBy(() -> bookingService.bookAppointment(bookingRequest))
                    .isInstanceOf(SlotNotAvailableException.class);

            verify(appointmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cancel Appointment Tests")
    class CancelAppointmentTests {

        @Test
        @DisplayName("Should successfully cancel a confirmed appointment")
        void shouldCancelAppointmentSuccessfully() {
            // Given
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

            // When
            CancellationConfirmation result = bookingService.cancelAppointment(appointmentId, "Personal reasons");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAppointmentId()).isEqualTo(appointmentId);
            assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELLED.name());

            verify(doctorServiceClient).releaseSlot(slotId);
            verify(eventPublisher).publishAppointmentCancelled(any());
        }

        @Test
        @DisplayName("Should throw exception when appointment not found")
        void shouldThrowExceptionWhenAppointmentNotFound() {
            // Given
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> bookingService.cancelAppointment(appointmentId, null))
                    .isInstanceOf(AppointmentNotFoundException.class);

            verify(doctorServiceClient, never()).releaseSlot(any());
        }

        @Test
        @DisplayName("Should throw exception when cancelling already cancelled appointment")
        void shouldThrowExceptionWhenCancellingAlreadyCancelledAppointment() {
            // Given
            appointment.setStatus(AppointmentStatus.CANCELLED);
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

            // When/Then
            assertThatThrownBy(() -> bookingService.cancelAppointment(appointmentId, null))
                    .isInstanceOf(AppointmentCancellationException.class);
        }
    }

    @Nested
    @DisplayName("Get Patient Appointments Tests")
    class GetPatientAppointmentsTests {

        @Test
        @DisplayName("Should return paginated appointments for patient")
        void shouldReturnPaginatedAppointments() {
            // Given
            Page<Appointment> page = new PageImpl<>(List.of(appointment), PageRequest.of(0, 10), 1);
            when(appointmentRepository.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(
                    eq(patientId), any())).thenReturn(page);
            when(appointmentMapper.toDetail(any())).thenReturn(
                    AppointmentDetail.builder()
                            .appointmentId(appointmentId)
                            .patientId(patientId)
                            .status(AppointmentStatus.CONFIRMED)
                            .build());

            // When
            PagedResponse<AppointmentDetail> result = bookingService.getPatientAppointments(patientId, 0, 10);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Get Doctor Today Appointments Tests")
    class GetDoctorTodayAppointmentsTests {

        @Test
        @DisplayName("Should return today's appointments for doctor")
        void shouldReturnTodaysAppointments() {
            // Given
            when(appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusOrderByStartTimeAsc(
                    eq(doctorId), any(LocalDate.class), eq(AppointmentStatus.CONFIRMED)))
                    .thenReturn(List.of(appointment));
            when(appointmentMapper.toDetail(any())).thenReturn(
                    AppointmentDetail.builder()
                            .appointmentId(appointmentId)
                            .doctorId(doctorId)
                            .status(AppointmentStatus.CONFIRMED)
                            .build());

            // When
            List<AppointmentDetail> result = bookingService.getDoctorTodayAppointments(doctorId);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Get Appointment Tests")
    class GetAppointmentTests {

        @Test
        @DisplayName("Should return appointment by ID")
        void shouldReturnAppointmentById() {
            // Given
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
            when(appointmentMapper.toDetail(appointment)).thenReturn(
                    AppointmentDetail.builder()
                            .appointmentId(appointmentId)
                            .patientId(patientId)
                            .doctorId(doctorId)
                            .status(AppointmentStatus.CONFIRMED)
                            .build());

            // When
            AppointmentDetail result = bookingService.getAppointment(appointmentId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAppointmentId()).isEqualTo(appointmentId);
        }

        @Test
        @DisplayName("Should throw exception when appointment not found")
        void shouldThrowExceptionWhenNotFound() {
            // Given
            when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> bookingService.getAppointment(appointmentId))
                    .isInstanceOf(AppointmentNotFoundException.class);
        }
    }
}
