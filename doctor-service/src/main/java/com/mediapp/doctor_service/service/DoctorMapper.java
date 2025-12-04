package com.mediapp.doctor_service.service;

import org.springframework.stereotype.Component;

import com.mediapp.doctor_service.api.dto.AvailabilitySlotResponse;
import com.mediapp.doctor_service.api.dto.DoctorProfileResponse;
import com.mediapp.doctor_service.api.dto.SlotReservationResponse;
import com.mediapp.doctor_service.domain.AvailabilitySlotEntity;
import com.mediapp.doctor_service.domain.DoctorProfileEntity;
import com.mediapp.doctor_service.domain.SpecialtyEntity;

/**
 * Centralized mapping logic between persistence entities and API payloads.
 */
@Component
public class DoctorMapper {

    public DoctorProfileResponse toDoctorProfileResponse(DoctorProfileEntity doctor, SpecialtyEntity specialty) {
        return DoctorProfileResponse.builder()
                .doctorId(doctor.getId())
                .medicalLicenseNumber(doctor.getMedicalLicenseNumber())
                .specialtyId(doctor.getSpecialtyId())
                .specialtyName(specialty != null ? specialty.getName() : null)
                .officeAddress(doctor.getOfficeAddress())
                .build();
    }

    public AvailabilitySlotResponse toAvailabilitySlotResponse(AvailabilitySlotEntity slot) {
        return AvailabilitySlotResponse.builder()
                .slotId(slot.getId())
                .doctorId(slot.getDoctorId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .reserved(slot.isReserved())
                .build();
    }

    public SlotReservationResponse toReservationResponse(AvailabilitySlotEntity slot) {
        return SlotReservationResponse.builder()
                .slotId(slot.getId())
                .doctorId(slot.getDoctorId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .reservedAt(slot.getReservedAt())
                .reservationToken(slot.getReservationToken())
                .build();
    }
}
