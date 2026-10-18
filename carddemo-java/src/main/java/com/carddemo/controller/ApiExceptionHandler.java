package com.carddemo.controller;

import com.carddemo.dto.ErrorResponse;
import com.carddemo.service.CardLookupException;
import com.carddemo.service.CardLookupMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(CardLookupException.class)
    public ResponseEntity<ErrorResponse> lookupFailed(CardLookupException e) {
        return body(e.getStatus(), e.getMessage(), e.getField());
    }

    /** COCRDSLC {@code WHEN OTHER} on the CARDDAT read: file error. */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> readFailed(DataAccessException e) {
        log.error("Card data read failed", e);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, CardLookupMessages.READ_ERROR, null);
    }

    private static ResponseEntity<ErrorResponse> body(HttpStatus status, String message, String field) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), status.getReasonPhrase(), message, field));
    }
}
