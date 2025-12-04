package com.mediapp.doctor_service.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

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

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        private final MeterRegistry meterRegistry;
        private final Clock clock;
        private final TransactionalOperator transactionalOperator;

        public DoctorAvailabilityService(DoctorProfileRepository doctorProfileRepository,
                        SpecialtyRepository specialtyRepository,
                        AvailabilitySlotRepository availabilitySlotRepository,
                        AvailabilitySlotGenerator slotGenerator,
                        DoctorMapper doctorMapper,
                        MeterRegistry meterRegistry,
                        Clock clock,
                        TransactionalOperator transactionalOperator) {
                this.doctorProfileRepository = doctorProfileRepository;
                this.specialtyRepository = specialtyRepository;
                this.availabilitySlotRepository = availabilitySlotRepository;
                this.slotGenerator = slotGenerator;
                this.doctorMapper = doctorMapper;
                this.meterRegistry = meterRegistry;
                this.clock = clock;
                this.transactionalOperator = transactionalOperator;
        }

        public Mono<List<DoctorProfileResponse>> findDoctorsBySpecialty(Integer specialtyId) {
                Timer.Sample sample = Timer.start(meterRegistry);
                String specialtyTag = String.valueOf(specialtyId);

                Mono<SpecialtyEntity> specialtyMono = specialtyRepository.findById(specialtyId)
                                .switchIfEmpty(Mono.error(new SpecialtyNotFoundException(specialtyId)));

                return specialtyMono.flatMapMany(specialty -> doctorProfileRepository.findBySpecialtyId(specialtyId)
                                .map(doctor -> doctorMapper.toDoctorProfileResponse(doctor, specialty)))
                                .collectList()
                                .doFinally(
                                                signal -> stopTimer(sample, "doctor.availability.search.latency",
                                                                "specialtyId", specialtyTag));
        }

        public Mono<List<AvailabilitySlotResponse>> getAvailability(String doctorId, Instant from, Instant to) {
                Timer.Sample sample = Timer.start(meterRegistry);
                Instant effectiveFrom = from != null ? from : Instant.now(clock);
                Instant effectiveTo = to != null ? to : effectiveFrom.plus(DEFAULT_LOOKAHEAD);

                return ensureDoctorExists(doctorId)
                                .thenMany(availabilitySlotRepository
                                                .findByDoctorIdAndStartTimeBetweenOrderByStartTimeAsc(doctorId,
                                                                effectiveFrom, effectiveTo))
                                .map(doctorMapper::toAvailabilitySlotResponse)
                                .collectList()
                                .doFinally(signal -> stopTimer(sample, "doctor.availability.fetch.latency", "doctorId",
                                                doctorId));
        }

        public Mono<List<AvailabilitySlotResponse>> createRecurringSlots(String doctorId,
                        CreateAvailabilityRequest request) {
                Timer.Sample sample = Timer.start(meterRegistry);

                return ensureDoctorExists(doctorId)
                                .flatMap(doctor -> transactionalOperator.transactional(Mono.defer(() -> {
                                        List<AvailabilitySlotEntity> generated = slotGenerator.generate(doctorId,
                                                        request);
                                        return Flux.fromIterable(generated)
                                                        .concatMap(slot -> ensureNoOverlap(doctorId, slot))
                                                        .collectList()
                                                        .flatMap(slots -> availabilitySlotRepository.saveAll(slots)
                                                                        .collectList());
                                })))
                                .map(slots -> slots.stream()
                                                .map(doctorMapper::toAvailabilitySlotResponse)
                                                .collect(Collectors.toList()))
                                .doOnSuccess(list -> meterRegistry
                                                .counter("doctor.availability.create", "outcome", "success")
                                                .increment())
                                .doOnError(error -> meterRegistry
                                                .counter("doctor.availability.create", "outcome", "failed")
                                                .increment())
                                .doFinally(signal -> stopTimer(sample, "doctor.availability.create.latency", "doctorId",
                                                doctorId));
        }

        /**
         * Creates a new doctor profile. Called by user-service when a doctor registers.
         *
         * @param doctorId             the UUID of the doctor (same as user ID from
         *                             user-service)
         * @param medicalLicenseNumber the doctor's medical license number
         * @param specialtyId          the specialty ID
         * @param officeAddress        the doctor's office address
         * @return Mono<DoctorProfileResponse> the created doctor profile
         */
        public Mono<DoctorProfileResponse> createDoctorProfile(String doctorId, String medicalLicenseNumber,
                        Integer specialtyId, String officeAddress) {
                Timer.Sample sample = Timer.start(meterRegistry);

                return specialtyRepository.findById(specialtyId)
                                .switchIfEmpty(Mono.error(new SpecialtyNotFoundException(specialtyId)))
                                .flatMap(specialty -> {
                                        DoctorProfileEntity profile = DoctorProfileEntity.builder()
                                                        .id(doctorId)
                                                        .medicalLicenseNumber(medicalLicenseNumber)
                                                        .specialtyId(specialtyId)
                                                        .officeAddress(officeAddress)
                                                        .createdAt(Instant.now(clock))
                                                        .updatedAt(Instant.now(clock))
                                                        .build();

                                        return doctorProfileRepository.save(profile)
                                                        .map(saved -> doctorMapper.toDoctorProfileResponse(saved,
                                                                        specialty));
                                })
                                .doOnSuccess(res -> meterRegistry.counter("doctor.profile.create", "outcome", "success")
                                                .increment())
                                .doOnError(error -> meterRegistry.counter("doctor.profile.create", "outcome", "failed")
                                                .increment())
                                .doFinally(signal -> stopTimer(sample, "doctor.profile.create.latency", "doctorId",
                                                doctorId));
        }

        public Mono<SlotReservationResponse> reserveSlot(String slotId, String reservationToken) {
                Timer.Sample sample = Timer.start(meterRegistry);
                Instant now = Instant.now(clock);

                return availabilitySlotRepository.findById(slotId)
                                .switchIfEmpty(Mono.error(new AvailabilitySlotNotFoundException(slotId)))
                                .flatMap(existing -> availabilitySlotRepository
                                                .reserveSlot(slotId, reservationToken, now)
                                                .switchIfEmpty(Mono.error(
                                                                new AvailabilityReservationConflictException(slotId))))
                                .map(doctorMapper::toReservationResponse)
                                .doOnSuccess(res -> meterRegistry
                                                .counter("doctor.availability.reserve", "outcome", "success")
                                                .increment())
                                .doOnError(error -> {
                                        if (error instanceof AvailabilityReservationConflictException) {
                                                meterRegistry.counter("doctor.availability.reserve", "outcome",
                                                                "conflict").increment();
                                        } else if (error instanceof AvailabilitySlotNotFoundException) {
                                                meterRegistry.counter("doctor.availability.reserve", "outcome",
                                                                "not_found").increment();
                                        } else {
                                                meterRegistry.counter("doctor.availability.reserve", "outcome",
                                                                "failed").increment();
                                        }
                                })
                                .doFinally(signal -> stopTimer(sample, "doctor.availability.reserve.latency", "slotId",
                                                slotId));
        }

        private Mono<DoctorProfileEntity> ensureDoctorExists(String doctorId) {
                return doctorProfileRepository.findById(doctorId)
                                .switchIfEmpty(Mono.error(new DoctorNotFoundException(doctorId)));
        }

        private Mono<AvailabilitySlotEntity> ensureNoOverlap(String doctorId, AvailabilitySlotEntity slot) {
                return availabilitySlotRepository
                                .existsOverlappingSlot(doctorId, slot.getStartTime(), slot.getEndTime())
                                .flatMap(overlap -> overlap
                                                ? Mono.<AvailabilitySlotEntity>error(
                                                                new AvailabilityOverlapException(doctorId,
                                                                                slot.getStartTime(), slot.getEndTime()))
                                                : Mono.just(slot));
        }

        private void stopTimer(Timer.Sample sample, String metricName, String... tags) {
                if (sample == null) {
                        return;
                }
                try {
                        Timer timer = meterRegistry.timer(metricName, tags);
                        sample.stop(timer);
                } catch (IllegalArgumentException ignored) {
                        // Micrometer enforces even number of tags; ignoring misconfiguration keeps flow
                        // intact.
                }
        }
}
