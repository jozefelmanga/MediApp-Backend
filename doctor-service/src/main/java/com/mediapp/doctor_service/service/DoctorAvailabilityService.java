package com.mediapp.doctor_service.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mediapp.doctor_service.api.dto.AvailabilitySlotResponse;
import com.mediapp.doctor_service.api.dto.CreateAvailabilityRequest;
import com.mediapp.doctor_service.api.dto.DoctorProfileResponse;
import com.mediapp.doctor_service.api.dto.SlotReservationResponse;
import com.mediapp.doctor_service.domain.AvailabilitySlotEntity;
import com.mediapp.doctor_service.domain.DoctorProfileEntity;
import com.mediapp.doctor_service.domain.SpecialtyEntity;
import com.mediapp.doctor_service.domain.exception.AvailabilityOverlapException;
import com.mediapp.doctor_service.domain.exception.AvailabilityReservationConflictException;
import com.mediapp.doctor_service.domain.exception.AvailabilitySlotNotFoundException;
import com.mediapp.doctor_service.domain.exception.DoctorNotFoundException;
import com.mediapp.doctor_service.domain.exception.SpecialtyNotFoundException;
import com.mediapp.doctor_service.repository.AvailabilitySlotRepository;
import com.mediapp.doctor_service.repository.DoctorProfileRepository;
import com.mediapp.doctor_service.repository.SpecialtyRepository;

/**
 * Encapsulates the core query and command flows for doctor availability.
 */
@Service
public class DoctorAvailabilityService {

        private static final Duration DEFAULT_LOOKAHEAD = Duration.ofDays(30);

        private final DoctorProfileRepository doctorProfileRepository;
        private final SpecialtyRepository specialtyRepository;
        private final AvailabilitySlotRepository availabilitySlotRepository;
        private final AvailabilitySlotGenerator slotGenerator;
        private final DoctorMapper doctorMapper;
        private final Clock clock;

        public DoctorAvailabilityService(DoctorProfileRepository doctorProfileRepository,
                        SpecialtyRepository specialtyRepository,
                        AvailabilitySlotRepository availabilitySlotRepository,
                        AvailabilitySlotGenerator slotGenerator,
                        DoctorMapper doctorMapper,
                        Clock clock) {
                this.doctorProfileRepository = doctorProfileRepository;
                this.specialtyRepository = specialtyRepository;
                this.availabilitySlotRepository = availabilitySlotRepository;
                this.slotGenerator = slotGenerator;
                this.doctorMapper = doctorMapper;
                this.clock = clock;
        }

        public List<DoctorProfileResponse> findDoctorsBySpecialty(Integer specialtyId) {
                SpecialtyEntity specialty = specialtyRepository.findById(specialtyId)
                                .orElseThrow(() -> new SpecialtyNotFoundException(specialtyId));

                return doctorProfileRepository.findBySpecialtyId(specialtyId).stream()
                                .map(doctor -> doctorMapper.toDoctorProfileResponse(doctor, specialty))
                                .collect(Collectors.toList());
        }

        public List<AvailabilitySlotResponse> getAvailability(Long doctorId, Instant from, Instant to) {
                ensureDoctorExists(doctorId);

                Instant effectiveFrom = from != null ? from : Instant.now(clock);
                Instant effectiveTo = to != null ? to : effectiveFrom.plus(DEFAULT_LOOKAHEAD);

                return availabilitySlotRepository
                                .findByDoctorIdAndStartTimeBetweenOrderByStartTimeAsc(doctorId, effectiveFrom,
                                                effectiveTo)
                                .stream()
                                .map(doctorMapper::toAvailabilitySlotResponse)
                                .collect(Collectors.toList());
        }

        @Transactional
        public List<AvailabilitySlotResponse> createRecurringSlots(Long doctorId, CreateAvailabilityRequest request) {
                ensureDoctorExists(doctorId);

                List<AvailabilitySlotEntity> generated = slotGenerator.generate(doctorId, request);

                // Check for overlaps
                for (AvailabilitySlotEntity slot : generated) {
                        ensureNoOverlap(doctorId, slot);
                }

                // Save all slots
                List<AvailabilitySlotEntity> saved = availabilitySlotRepository.saveAll(generated);

                return saved.stream()
                                .map(doctorMapper::toAvailabilitySlotResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Creates a new doctor profile. Called by user-service when a doctor registers.
         *
         * @param userId               the ID of the user from user-service
         * @param medicalLicenseNumber the doctor's medical license number
         * @param specialtyId          the specialty ID
         * @param officeAddress        the doctor's office address
         * @return DoctorProfileResponse the created doctor profile
         */
        @Transactional
        public DoctorProfileResponse createDoctorProfile(Long userId, String medicalLicenseNumber,
                        Integer specialtyId, String officeAddress) {
                SpecialtyEntity specialty = specialtyRepository.findById(specialtyId)
                                .orElseThrow(() -> new SpecialtyNotFoundException(specialtyId));

                DoctorProfileEntity profile = DoctorProfileEntity.builder()
                                .userId(userId)
                                .medicalLicenseNumber(medicalLicenseNumber)
                                .specialtyId(specialtyId)
                                .officeAddress(officeAddress)
                                .build();

                DoctorProfileEntity saved = doctorProfileRepository.save(profile);
                return doctorMapper.toDoctorProfileResponse(saved, specialty);
        }

        @Transactional
        public SlotReservationResponse reserveSlot(Long slotId) {
                // First check if slot exists
                AvailabilitySlotEntity existing = availabilitySlotRepository.findById(slotId)
                                .orElseThrow(() -> new AvailabilitySlotNotFoundException(slotId));

                // Check if already reserved
                if (existing.isReserved()) {
                        throw new AvailabilityReservationConflictException(slotId);
                }

                // Reserve the slot
                existing.setReserved(true);

                AvailabilitySlotEntity saved = availabilitySlotRepository.save(existing);
                return doctorMapper.toReservationResponse(saved);
        }

        /**
         * Releases a previously reserved slot (for cancellation/compensation
         * scenarios).
         *
         * @param slotId the slot ID to release
         * @return SlotReservationResponse the slot after release
         */
        @Transactional
        public SlotReservationResponse releaseSlot(Long slotId) {
                AvailabilitySlotEntity existing = availabilitySlotRepository.findById(slotId)
                                .orElseThrow(() -> new AvailabilitySlotNotFoundException(slotId));

                // Release the slot
                existing.setReserved(false);

                AvailabilitySlotEntity saved = availabilitySlotRepository.save(existing);
                return doctorMapper.toReservationResponse(saved);
        }

        private DoctorProfileEntity ensureDoctorExists(Long doctorId) {
                return doctorProfileRepository.findById(doctorId)
                                .orElseThrow(() -> new DoctorNotFoundException(doctorId));
        }

        private void ensureNoOverlap(Long doctorId, AvailabilitySlotEntity slot) {
                boolean overlap = availabilitySlotRepository
                                .existsOverlappingSlot(doctorId, slot.getStartTime(), slot.getEndTime());
                if (overlap) {
                        throw new AvailabilityOverlapException(doctorId, slot.getStartTime(), slot.getEndTime());
                }
        }
}
