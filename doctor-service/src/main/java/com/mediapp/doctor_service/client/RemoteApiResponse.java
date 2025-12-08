package com.mediapp.doctor_service.client;

/**
 * Lightweight representation of remote service API wrapper used by
 * user-service.
 */
public record RemoteApiResponse<T>(boolean success, T data, String message) {

    public static <T> RemoteApiResponse<T> success(T data) {
        return new RemoteApiResponse<>(true, data, null);
    }
}
