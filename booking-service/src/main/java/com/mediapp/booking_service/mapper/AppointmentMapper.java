package com.mediapp.booking_service.mapper;

import com.mediapp.booking_service.dto.AppointmentConfirmation;
import com.mediapp.booking_service.dto.AppointmentDetail;
import com.mediapp.booking_service.entity.Appointment;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Appointment entity and DTOs.
 */
@Component
public class AppointmentMapper {

    /**
     * Convert Appointment entity to AppointmentDetail DTO.
     *
     * @param appointment the entity
     * @return the detail DTO
     */
    public AppointmentDetail toDetail(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        return AppointmentDetail.builder()
                .appointmentId(appointment.getAppointmentId())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .slotId(appointment.getSlotId())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .status(appointment.getStatus())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }

    /**
     * Convert Appointment entity to AppointmentConfirmation DTO.
     *
     * @param appointment the entity
     * @param message     confirmation message
     * @return the confirmation DTO
     */
    public AppointmentConfirmation toConfirmation(Appointment appointment, String message) {
        if (appointment == null) {
            return null;
        }

        return AppointmentConfirmation.builder()
                .appointmentId(appointment.getAppointmentId())
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .slotId(appointment.getSlotId())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .status(appointment.getStatus())
                .confirmedAt(appointment.getCreatedAt())
                .message(message)
                .build();
    }
}
