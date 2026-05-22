package com.financecontrol.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final String ERROR_INTERNAL = "error.internal";
    private final MessageSource messageSource;

    private ResponseEntity<Map<String, String>> error(@NonNull HttpStatus status, String msgKey) {
        String key = msgKey != null ? msgKey : ERROR_INTERNAL;
        String message;
        try {
            message = messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            message = key;
        }
        return ResponseEntity.status(status).body(Map.of("message", message, "errorCode", key));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusiness(BusinessException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException e) {
        return error(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoResource() {
        return error(HttpStatus.NOT_FOUND, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception e) {
        log.error("Erro inesperado: ", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_INTERNAL);
    }
}
