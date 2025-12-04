package com.mediapp.notification_service.service;

import com.mediapp.notification_service.dto.AppointmentCancelledEvent;
import com.mediapp.notification_service.dto.AppointmentCreatedEvent;

public interface NotificationService {
    void handleAppointmentCreated(AppointmentCreatedEvent event);

    void handleAppointmentCancelled(AppointmentCancelledEvent event);
}
