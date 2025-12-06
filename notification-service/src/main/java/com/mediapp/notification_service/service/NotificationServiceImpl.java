package com.mediapp.notification_service.service;

import com.mediapp.notification_service.dto.AppointmentCancelledEvent;
import com.mediapp.notification_service.dto.AppointmentCreatedEvent;
import com.mediapp.notification_service.entity.NotificationLog;
import com.mediapp.notification_service.entity.NotificationStatus;
import com.mediapp.notification_service.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationLogRepository repository;

    /**
     * Handle an appointment created event: persist log, dedupe by eventId, and
     * simulate sending notification.
     */
    @Override
    @Transactional
    public void handleAppointmentCreated(AppointmentCreatedEvent event) {
        Long eventId = event.getEventId();
        if (eventId == null) {
            log.warn("Received AppointmentCreatedEvent without eventId, generating one");
        }
        if (repository.existsByEventId(eventId)) {
            log.info("Duplicate AppointmentCreatedEvent received, eventId={}, skipping", eventId);
            return;
        }

        NotificationLog logEntry = NotificationLog.builder()
                .eventId(eventId)
                .recipientUserId(event.getPatientId())
                .messageType("APPOINTMENT_CREATED")
                .status(NotificationStatus.PENDING)
                .build();

        repository.save(logEntry);

        // Simulate delivery
        try {
            sendNotification(logEntry);
            logEntry.setStatus(NotificationStatus.SENT);
            logEntry.setSentAt(LocalDateTime.now());
            repository.save(logEntry);
            log.info("Notification sent for eventId={}", eventId);
        } catch (Exception ex) {
            log.error("Failed to send notification for eventId={}", eventId, ex);
            logEntry.setStatus(NotificationStatus.FAILED);
            repository.save(logEntry);
        }
    }

    @Override
    @Transactional
    public void handleAppointmentCancelled(AppointmentCancelledEvent event) {
        Long eventId = event.getEventId();
        if (repository.existsByEventId(eventId)) {
            log.info("Duplicate AppointmentCancelledEvent received, eventId={}, skipping", eventId);
            return;
        }

        NotificationLog logEntry = NotificationLog.builder()
                .eventId(eventId)
                .recipientUserId(event.getPatientId())
                .messageType("APPOINTMENT_CANCELLED")
                .status(NotificationStatus.PENDING)
                .build();

        repository.save(logEntry);

        // Simulate delivery
        try {
            sendNotification(logEntry);
            logEntry.setStatus(NotificationStatus.SENT);
            logEntry.setSentAt(LocalDateTime.now());
            repository.save(logEntry);
            log.info("Cancellation notification sent for eventId={}", eventId);
        } catch (Exception ex) {
            log.error("Failed to send cancellation notification for eventId={}", eventId, ex);
            logEntry.setStatus(NotificationStatus.FAILED);
            repository.save(logEntry);
        }
    }

    /**
     * Simulates sending a notification. Replace this with real provider
     * integration.
     */
    private void sendNotification(NotificationLog logEntry) {
        // Placeholder: integrate with email/SMS provider here.
        log.debug("Sending notification to user={} type={}",
                logEntry.getRecipientUserId(), logEntry.getMessageType());
    }
}
