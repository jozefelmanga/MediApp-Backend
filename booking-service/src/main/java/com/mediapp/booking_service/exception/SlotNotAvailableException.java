package com.mediapp.booking_service.exception;

/**
 * Exception thrown when a slot is not available for booking.
 */
public class SlotNotAvailableException extends RuntimeException {

    public SlotNotAvailableException(String message) {
        super(message);
    }

    public static SlotNotAvailableException forSlot(Long slotId) {
        return new SlotNotAvailableException("Slot is not available or already booked: " + slotId);
    }
}
