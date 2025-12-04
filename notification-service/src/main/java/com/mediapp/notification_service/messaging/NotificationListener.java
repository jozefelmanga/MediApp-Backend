package com.mediapp.notification_service.messaging;

import com.mediapp.notification_service.dto.AppointmentCancelledEvent;
import com.mediapp.notification_service.dto.AppointmentCreatedEvent;
import com.mediapp.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ listeners to consume appointment events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "appointment-created")
    public void onAppointmentCreated(@Payload AppointmentCreatedEvent event) {
        log.info("Received AppointmentCreatedEvent eventId={}", event != null ? event.getEventId() : null);
        try {
            notificationService.handleAppointmentCreated(event);
        } catch (Exception ex) {
            log.error("Error processing AppointmentCreatedEvent eventId={}", event != null ? event.getEventId() : null,
                    ex);
            throw ex; // allow requeue/retry policies if configured
        }
    }

    @RabbitListener(queues = "appointment-cancelled")
    public void onAppointmentCancelled(@Payload AppointmentCancelledEvent event) {
        log.info("Received AppointmentCancelledEvent eventId={}", event != null ? event.getEventId() : null);
        try {
            notificationService.handleAppointmentCancelled(event);
        } catch (Exception ex) {
            log.error("Error processing AppointmentCancelledEvent eventId={}",
                    event != null ? event.getEventId() : null, ex);
            throw ex;
        }
    }
}
