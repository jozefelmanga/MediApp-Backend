package com.mediapp.booking_service.exception;

import java.util.UUID;

/**
 * Exception thrown when a slot is not available for booking.
 */
public class SlotNotAvailableException extends RuntimeException {

    public SlotNotAvailableException(UUID slotId) {
        super("Slot is not available or already booked: " + slotId);
    }

    public SlotNotAvailableException(String message) {
        super(message);
    }
}
