package com.mediapp.doctor_service.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import com.mediapp.common.error.CommonErrorCode;
import com.mediapp.common.error.DomainException;
import com.mediapp.common.error.ErrorDetail;
import com.mediapp.common.error.ErrorResponse;

import reactor.core.publisher.Mono;

/**
 * Translates exceptions into the shared error response structure for WebFlux
 * controllers.
 */
@RestControllerAdvice
public class ReactiveExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    public ReactiveExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(DomainException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDomainException(DomainException exception,
            ServerWebExchange exchange) {
        String correlationId = correlationId(exchange);
        String message = resolveMessage(exception.getErrorCode().messageKey(), exception.getMessage());
        ErrorResponse body = ErrorResponse.from(exception.getErrorCode(), message, correlationId,
                exception.getDetails());
        return Mono.just(ResponseEntity.status(exception.getErrorCode().httpStatus()).body(body));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUnhandled(Exception exception, ServerWebExchange exchange) {
        String correlationId = correlationId(exchange);
        ErrorResponse body = ErrorResponse.from(
                CommonErrorCode.INTERNAL_ERROR,
                resolveMessage(CommonErrorCode.INTERNAL_ERROR.messageKey(), exception.getMessage()),
                correlationId,
                null);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body));
    }

    @Override
    protected Mono<ResponseEntity<Object>> handleWebExchangeBindException(
            org.springframework.web.bind.support.WebExchangeBindException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            ServerWebExchange exchange) {
        List<ErrorDetail> details = new ArrayList<>();
        details.addAll(ex.getFieldErrors().stream()
                .map(error -> new ErrorDetail(
                        CommonErrorCode.VALIDATION_FAILED.code(),
                        resolveFieldErrorMessage(error),
                        error.getField(),
                        error.getRejectedValue()))
                .collect(Collectors.toList()));
        details.addAll(ex.getGlobalErrors().stream()
                .map(error -> new ErrorDetail(
                        CommonErrorCode.VALIDATION_FAILED.code(),
                        resolveObjectErrorMessage(error),
                        error.getObjectName()))
                .collect(Collectors.toList()));

        String correlationId = correlationId(exchange);
        ErrorResponse body = ErrorResponse.from(
                CommonErrorCode.VALIDATION_FAILED,
                resolveMessage(CommonErrorCode.VALIDATION_FAILED.messageKey(), "Validation failed"),
                correlationId,
                details);
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
    }

    @Override
    protected Mono<ResponseEntity<Object>> handleServerWebInputException(ServerWebInputException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            ServerWebExchange exchange) {
        String correlationId = correlationId(exchange);
        ErrorResponse body = ErrorResponse.from(
                CommonErrorCode.VALIDATION_FAILED,
                resolveMessage(CommonErrorCode.VALIDATION_FAILED.messageKey(), ex.getReason()),
                correlationId,
                null);
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
    }

    private String resolveMessage(String messageKey, @Nullable String defaultMessage) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(messageKey, null, Optional.ofNullable(defaultMessage).orElse(messageKey),
                locale);
    }

    private String resolveFieldErrorMessage(FieldError error) {
        String[] codes = error.getCodes();
        if (codes != null) {
            for (String code : codes) {
                String resolved = messageSource.getMessage(code, error.getArguments(), null,
                        LocaleContextHolder.getLocale());
                if (resolved != null) {
                    return resolved;
                }
            }
        }
        return Optional.ofNullable(error.getDefaultMessage()).orElse(error.getField());
    }

    private String resolveObjectErrorMessage(ObjectError error) {
        String[] codes = error.getCodes();
        if (codes != null) {
            for (String code : codes) {
                String resolved = messageSource.getMessage(code, error.getArguments(), null,
                        LocaleContextHolder.getLocale());
                if (resolved != null) {
                    return resolved;
                }
            }
        }
        return Optional.ofNullable(error.getDefaultMessage()).orElse(error.getObjectName());
    }

    private String correlationId(ServerWebExchange exchange) {
        return exchange.getAttribute(CorrelationIdWebFilter.CORRELATION_ID_ATTRIBUTE);
    }
}
