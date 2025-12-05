package com.mediapp.booking_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mediapp.booking_service.dto.*;
import com.mediapp.booking_service.entity.AppointmentStatus;
import com.mediapp.booking_service.exception.AppointmentNotFoundException;
import com.mediapp.booking_service.exception.GlobalExceptionHandler;
import com.mediapp.booking_service.exception.SlotNotAvailableException;
import com.mediapp.booking_service.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

        private MockMvc mockMvc;

        @Mock
        private BookingService bookingService;

        @InjectMocks
        private AppointmentController appointmentController;

        private ObjectMapper objectMapper;
        private UUID patientId;
        private UUID doctorId;
        private String slotId;
        private UUID appointmentId;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.standaloneSetup(appointmentController)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .build();

                objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                patientId = UUID.randomUUID();
                doctorId = UUID.randomUUID();
                slotId = UUID.randomUUID().toString();
                appointmentId = UUID.randomUUID();
        }

        @Test
        @DisplayName("POST /book - Should create appointment successfully")
        void shouldCreateAppointmentSuccessfully() throws Exception {
                // Given
                BookingRequest request = BookingRequest.builder()
                                .patientId(patientId)
                                .doctorId(doctorId)
                                .slotId(slotId)
                                .appointmentDate(LocalDate.now().plusDays(1))
                                .startTime(LocalTime.of(10, 0))
                                .build();

                AppointmentConfirmation confirmation = AppointmentConfirmation.builder()
                                .appointmentId(appointmentId)
                                .patientId(patientId)
                                .doctorId(doctorId)
                                .slotId(slotId)
                                .status(AppointmentStatus.CONFIRMED)
                                .confirmedAt(LocalDateTime.now())
                                .message("Appointment successfully booked")
                                .build();

                when(bookingService.bookAppointment(any(BookingRequest.class))).thenReturn(confirmation);

                // When/Then
                mockMvc.perform(post("/api/v1/appointments/book")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.appointmentId").exists())
                                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                                .andExpect(jsonPath("$.message").value("Appointment successfully booked"));
        }

        @Test
        @DisplayName("POST /book - Should return 409 when slot not available")
        void shouldReturn409WhenSlotNotAvailable() throws Exception {
                // Given
                BookingRequest request = BookingRequest.builder()
                                .patientId(patientId)
                                .doctorId(doctorId)
                                .slotId(slotId)
                                .appointmentDate(LocalDate.now().plusDays(1))
                                .startTime(LocalTime.of(10, 0))
                                .build();

                when(bookingService.bookAppointment(any(BookingRequest.class)))
                                .thenThrow(SlotNotAvailableException.forSlot(slotId));

                // When/Then
                mockMvc.perform(post("/api/v1/appointments/book")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("PUT /cancel/{id} - Should cancel appointment successfully")
        void shouldCancelAppointmentSuccessfully() throws Exception {
                // Given
                CancellationConfirmation confirmation = CancellationConfirmation.builder()
                                .appointmentId(appointmentId)
                                .status("CANCELLED")
                                .cancelledAt(LocalDateTime.now())
                                .message("Appointment successfully cancelled")
                                .build();

                when(bookingService.cancelAppointment(eq(appointmentId), any())).thenReturn(confirmation);

                // When/Then
                mockMvc.perform(put("/api/v1/appointments/cancel/{appointmentId}", appointmentId)
                                .param("reason", "Personal reasons"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.appointmentId").value(appointmentId.toString()))
                                .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("GET /patient/{patientId} - Should return patient appointments")
        void shouldReturnPatientAppointments() throws Exception {
                // Given
                AppointmentDetail detail = AppointmentDetail.builder()
                                .appointmentId(appointmentId)
                                .patientId(patientId)
                                .doctorId(doctorId)
                                .status(AppointmentStatus.CONFIRMED)
                                .build();

                PagedResponse<AppointmentDetail> response = PagedResponse.<AppointmentDetail>builder()
                                .content(List.of(detail))
                                .page(0)
                                .size(10)
                                .totalElements(1)
                                .totalPages(1)
                                .first(true)
                                .last(true)
                                .build();

                when(bookingService.getPatientAppointments(eq(patientId), anyInt(), anyInt())).thenReturn(response);

                // When/Then
                mockMvc.perform(get("/api/v1/appointments/patient/{patientId}", patientId)
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content[0].appointmentId").value(appointmentId.toString()))
                                .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("GET /doctor/{doctorId}/today - Should return doctor's today appointments")
        void shouldReturnDoctorTodayAppointments() throws Exception {
                // Given
                AppointmentDetail detail = AppointmentDetail.builder()
                                .appointmentId(appointmentId)
                                .patientId(patientId)
                                .doctorId(doctorId)
                                .appointmentDate(LocalDate.now())
                                .status(AppointmentStatus.CONFIRMED)
                                .build();

                when(bookingService.getDoctorTodayAppointments(doctorId)).thenReturn(List.of(detail));

                // When/Then
                mockMvc.perform(get("/api/v1/appointments/doctor/{doctorId}/today", doctorId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].doctorId").value(doctorId.toString()));
        }

        @Test
        @DisplayName("GET /{appointmentId} - Should return appointment by ID")
        void shouldReturnAppointmentById() throws Exception {
                // Given
                AppointmentDetail detail = AppointmentDetail.builder()
                                .appointmentId(appointmentId)
                                .patientId(patientId)
                                .doctorId(doctorId)
                                .status(AppointmentStatus.CONFIRMED)
                                .build();

                when(bookingService.getAppointment(appointmentId)).thenReturn(detail);

                // When/Then
                mockMvc.perform(get("/api/v1/appointments/{appointmentId}", appointmentId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.appointmentId").value(appointmentId.toString()));
        }

        @Test
        @DisplayName("GET /{appointmentId} - Should return 404 when not found")
        void shouldReturn404WhenAppointmentNotFound() throws Exception {
                // Given
                when(bookingService.getAppointment(appointmentId))
                                .thenThrow(new AppointmentNotFoundException(appointmentId));

                // When/Then
                mockMvc.perform(get("/api/v1/appointments/{appointmentId}", appointmentId))
                                .andExpect(status().isNotFound());
        }
}
