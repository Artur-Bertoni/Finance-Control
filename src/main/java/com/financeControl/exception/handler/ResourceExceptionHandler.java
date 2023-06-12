package com.financeControl.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
@RequiredArgsConstructor
public class ResourceExceptionHandler {

    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    void exceptionHandler(ValidationException e) {
        throw new ResponseStatusException(BAD_REQUEST, e.getMessage(), e);
    }

    @ResponseBody
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorDTO> handlerResponseStatus(ResponseStatusException e, HttpServletRequest request) {
        return new ResponseEntity<>(ErrorDTO.builder()
                .message(e.getReason())
                .error(HttpStatusCode.valueOf(e.getBody().getStatus()).toString())
                .path(request.getRequestURI())
                .status(e.getBody().getStatus())
                .build(), HttpStatusCode.valueOf(e.getBody().getStatus()));
    }
}
