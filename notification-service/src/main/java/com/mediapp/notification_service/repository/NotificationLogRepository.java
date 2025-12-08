package com.mediapp.notification_service.repository;

import com.mediapp.notification_service.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    Optional<NotificationLog> findByEventId(Long eventId);

    boolean existsByEventId(Long eventId);

    java.util.List<NotificationLog> findAllByRecipientUserId(Long recipientUserId);

    java.util.List<NotificationLog> findAllByRecipientUserIdAndReadFalse(Long recipientUserId);
}
