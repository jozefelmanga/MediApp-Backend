package com.mediapp.gateway_service.filters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Normalizes error payloads bubbling up through the gateway so client
 * applications receive
 * a consistent JSON structure regardless of the upstream failure source.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorHandlingFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
                .onErrorResume(throwable -> handleError(exchange, throwable));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private Mono<Void> handleError(ServerWebExchange exchange, Throwable throwable) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(throwable);
        }

        HttpStatusCode status = resolveStatus(throwable);
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = buildBody(exchange, status, throwable);
        byte[] payload;
        try {
            payload = objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException jsonProcessingException) {
            log.error("Failed to serialize error response", jsonProcessingException);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            payload = ("{\"message\":\"Gateway error\"}").getBytes(StandardCharsets.UTF_8);
        }

        return response.writeWith(Mono.just(response.bufferFactory().wrap(payload)));
    }

    private HttpStatusCode resolveStatus(Throwable throwable) {
        if (throwable instanceof ResponseStatusException statusException) {
            return statusException.getStatusCode();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private Map<String, Object> buildBody(ServerWebExchange exchange, HttpStatusCode status, Throwable throwable) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("path", exchange.getRequest().getURI().getPath());
        body.put("status", status.value());
        body.put("error", status instanceof HttpStatus httpStatus ? httpStatus.getReasonPhrase() : "Unexpected Error");
        body.put("message", Optional.ofNullable(throwable.getMessage())
                .filter(message -> !message.isBlank())
                .orElse("Unexpected error encountered while routing the request."));
        body.put("requestId", exchange.getRequest().getHeaders()
                .getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER));
        return body;
    }
}
