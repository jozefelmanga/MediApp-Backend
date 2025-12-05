package com.mediapp.booking_service.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper for API responses from doctor-service.
 * Doctor-service wraps all responses in this format.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorServiceApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
}
