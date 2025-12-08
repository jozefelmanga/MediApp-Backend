package com.mediapp.gateway_service.filter;

import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

/**
 * Global gateway filter that extracts the JWT subject (auth user id) and
 * propagates it as `X-Auth-User-Id` header to downstream services.
 *
 * Note: This implementation parses the JWT without validating signature.
 * For production, consider verifying the token signature using the
 * security-service public key.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthUserIdPropagationFilter implements GlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthUserIdPropagationFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            try {
                SignedJWT jwt = SignedJWT.parse(token);
                String subject = jwt.getJWTClaimsSet().getSubject();
                if (subject != null && !subject.isBlank()) {
                    ServerHttpRequest mutated = exchange.getRequest().mutate()
                            .header("X-Auth-User-Id", subject)
                            .build();
                    ServerWebExchange mutatedExchange = exchange.mutate().request(mutated).build();
                    return chain.filter(mutatedExchange);
                }
            } catch (Exception e) {
                log.debug("Failed to parse JWT or extract subject: {}", e.getMessage());
                // fallthrough - continue without adding header
            }
        }
        return chain.filter(exchange);
    }
}
