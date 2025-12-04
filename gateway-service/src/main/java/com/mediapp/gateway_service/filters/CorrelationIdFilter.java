package com.mediapp.gateway_service.filters;


import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Adds or propagates a correlation identifier so downstream services and logs
 * share
 * the same request context. Also logs high-level request/response details.
 */
@Slf4j
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = resolveCorrelationId(exchange);

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .headers(headers -> headers.set(CORRELATION_ID_HEADER, correlationId))
                .build();

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().set(CORRELATION_ID_HEADER, correlationId);

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
        logRequest(mutatedRequest, correlationId);

        return chain.filter(mutatedExchange)
                .doOnSuccess(unused -> logResponse(mutatedExchange, correlationId, null))
                .doOnError(throwable -> logResponse(mutatedExchange, correlationId, throwable));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private String resolveCorrelationId(ServerWebExchange exchange) {
        return Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER))
                .filter(StringUtils::hasText)
                .orElseGet(() -> UUID.randomUUID().toString());
    }

    private void logRequest(ServerHttpRequest request, String correlationId) {
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        log.info("Incoming request [{} {}] correlationId={} from={}",
                request.getMethod(), request.getURI().getPath(), correlationId,
                remoteAddress != null ? remoteAddress.getHostString() : "unknown");
    }

    private void logResponse(ServerWebExchange exchange, String correlationId, Throwable error) {
        int statusCode = exchange.getResponse().getStatusCode() != null
                ? exchange.getResponse().getStatusCode().value()
                : 500;

        if (error == null) {
            log.info("Response status={} correlationId={} path={}", statusCode, correlationId,
                    exchange.getRequest().getURI().getPath());
        } else {
            log.error("Response status={} correlationId={} path={} error={}", statusCode, correlationId,
                    exchange.getRequest().getURI().getPath(), error.getMessage(), error);
        }
    }
}
