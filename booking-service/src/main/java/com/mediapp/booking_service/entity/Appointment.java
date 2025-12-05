package com.mediapp.booking_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Entity representing an appointment booking.
 * Links a patient to a doctor's availability slot.
 */
@Entity
@Table(name = "appointment", indexes = {
        @Index(name = "idx_appointment_patient", columnList = "patient_id"),
        @Index(name = "idx_appointment_doctor", columnList = "doctor_id"),
        @Index(name = "idx_appointment_slot", columnList = "slot_id"),
        @Index(name = "idx_appointment_date_status", columnList = "appointment_date, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "appointment_id", updatable = false, nullable = false)
    private UUID appointmentId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @Column(name = "slot_id", nullable = false, unique = true)
    private String slotId;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Cancels the appointment if it's currently in CONFIRMED or PENDING status.
     * 
     * @return true if cancellation was successful, false otherwise
     */
    public boolean cancel() {
        if (this.status == AppointmentStatus.CONFIRMED || this.status == AppointmentStatus.PENDING) {
            this.status = AppointmentStatus.CANCELLED;
            return true;
        }
        return false;
    }

    /**
     * Confirms the appointment if it's currently in PENDING status.
     * 
     * @return true if confirmation was successful, false otherwise
     */
    public boolean confirm() {
        if (this.status == AppointmentStatus.PENDING) {
            this.status = AppointmentStatus.CONFIRMED;
            return true;
        }
        return false;
    }
}
