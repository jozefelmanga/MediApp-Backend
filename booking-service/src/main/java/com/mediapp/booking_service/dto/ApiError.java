package com.mediapp.booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API error response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiError {

    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
}
