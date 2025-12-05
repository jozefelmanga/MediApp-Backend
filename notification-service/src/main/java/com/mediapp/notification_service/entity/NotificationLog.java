package com.mediapp.notification_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Persisted notification log to support idempotency and audit.
 */
@Entity
@Table(name = "notification_log", indexes = {
        @Index(name = "idx_notification_event", columnList = "event_id"),
        @Index(name = "idx_notification_recipient", columnList = "recipient_user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id", updatable = false, nullable = false)
    private Long logId;

    @Column(name = "event_id", nullable = false, unique = true)
    private Long eventId;

    @Column(name = "recipient_user_id")
    private Long recipientUserId;

    @Column(name = "message_type")
    private String messageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private NotificationStatus status;

    @Column(name = "payload", columnDefinition = "text")
    private String payload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "attempts")
    private int attempts;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;
}
