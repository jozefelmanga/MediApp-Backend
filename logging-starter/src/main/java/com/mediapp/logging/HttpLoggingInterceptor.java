package com.mediapp.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Simple interceptor that logs inbound HTTP requests when enabled.
 */
public class HttpLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HttpLoggingInterceptor.class);

    private final RequestLoggingProperties properties;

    public HttpLoggingInterceptor(RequestLoggingProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (properties.isLogInboundRequests()) {
            log.info("{} {}", request.getMethod(), request.getRequestURI());
        }
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            @Nullable Exception ex) {
        if (properties.isLogInboundRequests()) {
            log.info("{} {} -> {}", request.getMethod(), request.getRequestURI(), response.getStatus());
        }
    }
}
