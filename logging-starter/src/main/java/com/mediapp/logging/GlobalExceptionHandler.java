package com.mediapp.logging;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.mediapp.common.error.CommonErrorCode;
import com.mediapp.common.error.DomainException;
import com.mediapp.common.error.ErrorDetail;
import com.mediapp.common.error.ErrorResponse;

import jakarta.validation.ConstraintViolationException;

/**
 * Translates exceptions into the shared {@link ErrorResponse} format.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex) {
        String message = resolveMessage(ex.getErrorCode().messageKey(), ex.getMessage());
        ErrorResponse body = ErrorResponse.from(
                ex.getErrorCode(),
                message,
                currentCorrelationId(),
                ex.getDetails());
        return ResponseEntity.status(ex.getErrorCode().httpStatus()).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<ErrorDetail> details = ex.getConstraintViolations().stream()
                .map(violation -> new ErrorDetail(
                        CommonErrorCode.VALIDATION_FAILED.code(),
                        violation.getMessage(),
                        violation.getPropertyPath().toString(),
                        violation.getInvalidValue()))
                .collect(Collectors.toList());

        ErrorResponse body = ErrorResponse.from(
                CommonErrorCode.VALIDATION_FAILED,
                resolveMessage(CommonErrorCode.VALIDATION_FAILED.messageKey(), "Validation failed"),
                currentCorrelationId(),
                details);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandled(Exception ex) {
        ErrorResponse body = ErrorResponse.from(
                CommonErrorCode.INTERNAL_ERROR,
                resolveMessage(CommonErrorCode.INTERNAL_ERROR.messageKey(), ex.getMessage()),
                currentCorrelationId(),
                null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<ErrorDetail> details = fieldErrors.stream()
                .map(error -> new ErrorDetail(
                        CommonErrorCode.VALIDATION_FAILED.code(),
                        resolveFieldErrorMessage(error),
                        error.getField(),
                        error.getRejectedValue()))
                .collect(Collectors.toList());

        ErrorResponse body = ErrorResponse.from(
                CommonErrorCode.VALIDATION_FAILED,
                resolveMessage(CommonErrorCode.VALIDATION_FAILED.messageKey(), "Validation failed"),
                currentCorrelationId(),
                details);
        return handleExceptionInternal(ex, body, headers, HttpStatus.BAD_REQUEST, request);
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

    private String currentCorrelationId() {
        return MDC.get(CorrelationIdFilter.MDC_KEY);
    }
}
