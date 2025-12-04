package com.mediapp.doctor_service.config;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.mediapp.logging.RequestLoggingProperties;

import reactor.core.publisher.Mono;

/**
 * Minimal correlation id propagation tailored for WebFlux.
 */
@Component
public class CorrelationIdWebFilter implements WebFilter {

    public static final String MDC_KEY = "correlationId";
    public static final String CORRELATION_ID_ATTRIBUTE = CorrelationIdWebFilter.class.getName() + ".CORRELATION_ID";

    private final RequestLoggingProperties properties;

    public CorrelationIdWebFilter(RequestLoggingProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String headerName = properties.getCorrelationHeader();
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = resolveCorrelationId(request, headerName);

        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().set(headerName, correlationId);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(builder -> builder.headers(httpHeaders -> httpHeaders.set(headerName, correlationId)))
                .build();
        mutatedExchange.getAttributes().put(CORRELATION_ID_ATTRIBUTE, correlationId);

        return Mono.defer(() -> {
            MDC.put(MDC_KEY, correlationId);
            return chain.filter(mutatedExchange)
                    .doFinally(signalType -> MDC.remove(MDC_KEY));
        });
    }

    private String resolveCorrelationId(ServerHttpRequest request, String headerName) {
        return Optional.ofNullable(request.getHeaders().getFirst(headerName))
                .filter(StringUtils::hasText)
                .orElseGet(() -> UUID.randomUUID().toString());
    }
}
