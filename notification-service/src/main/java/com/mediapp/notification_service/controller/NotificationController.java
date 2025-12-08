package com.mediapp.notification_service.controller;

import com.mediapp.notification_service.entity.NotificationLog;
import com.mediapp.notification_service.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationLogRepository repository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationLog>> getNotificationsForUser(@PathVariable("userId") Long userId) {
        List<NotificationLog> notifications = repository.findAllByRecipientUserId(userId);
        if (notifications == null || notifications.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationLog>> getUnreadNotificationsForUser(@PathVariable("userId") Long userId) {
        List<NotificationLog> notifications = repository.findAllByRecipientUserIdAndReadFalse(userId);
        if (notifications == null || notifications.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(notifications);
    }

    @org.springframework.web.bind.annotation.RequestMapping(value = "/{id}/read", method = {
            org.springframework.web.bind.annotation.RequestMethod.POST,
            org.springframework.web.bind.annotation.RequestMethod.PUT })
    public ResponseEntity<NotificationLog> markAsRead(@PathVariable("id") Long id) {
        return repository.findById(id)
                .map(n -> {
                    n.setRead(Boolean.TRUE);
                    n.setReadAt(java.time.LocalDateTime.now());
                    NotificationLog saved = repository.save(n);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
