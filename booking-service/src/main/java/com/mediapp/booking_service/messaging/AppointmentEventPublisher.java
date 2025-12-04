package com.mediapp.booking_service.messaging;

import com.mediapp.booking_service.event.AppointmentCancelledEvent;
import com.mediapp.booking_service.event.AppointmentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Publisher for appointment events to RabbitMQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.appointment}")
    private String appointmentExchange;

    @Value("${rabbitmq.routing-key.appointment-created}")
    private String appointmentCreatedRoutingKey;

    @Value("${rabbitmq.routing-key.appointment-cancelled}")
    private String appointmentCancelledRoutingKey;

    /**
     * Publish an appointment created event.
     *
     * @param event the event to publish
     */
    public void publishAppointmentCreated(AppointmentCreatedEvent event) {
        log.info("Publishing AppointmentCreatedEvent for appointment: {}", event.getAppointmentId());
        try {
            rabbitTemplate.convertAndSend(appointmentExchange, appointmentCreatedRoutingKey, event);
            log.debug("Successfully published AppointmentCreatedEvent: {}", event);
        } catch (Exception e) {
            log.error("Failed to publish AppointmentCreatedEvent for appointment: {}",
                    event.getAppointmentId(), e);
            throw new RuntimeException("Failed to publish appointment created event", e);
        }
    }

    /**
     * Publish an appointment cancelled event.
     *
     * @param event the event to publish
     */
    public void publishAppointmentCancelled(AppointmentCancelledEvent event) {
        log.info("Publishing AppointmentCancelledEvent for appointment: {}", event.getAppointmentId());
        try {
            rabbitTemplate.convertAndSend(appointmentExchange, appointmentCancelledRoutingKey, event);
            log.debug("Successfully published AppointmentCancelledEvent: {}", event);
        } catch (Exception e) {
            log.error("Failed to publish AppointmentCancelledEvent for appointment: {}",
                    event.getAppointmentId(), e);
            throw new RuntimeException("Failed to publish appointment cancelled event", e);
        }
    }
}
