package com.carddemo.service;

import org.springframework.http.HttpStatus;

/** A COCRDSLC error path: HTTP status plus the legacy {@code WS-RETURN-MSG} text. */
public class CardLookupException extends RuntimeException {

    private final HttpStatus status;
    private final String field;

    public CardLookupException(HttpStatus status, String message, String field) {
        super(message);
        this.status = status;
        this.field = field;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getField() {
        return field;
    }
}
