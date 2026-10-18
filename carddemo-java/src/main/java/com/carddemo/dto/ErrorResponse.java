package com.carddemo.dto;

/**
 * Error body.
 *
 * @param status  HTTP status code
 * @param error   HTTP reason phrase
 * @param message the COBOL program's {@code WS-RETURN-MSG} text for the same condition
 * @param field   the input that failed ({@code acctId} or {@code cardNum}), if any
 */
public record ErrorResponse(int status, String error, String message, String field) {
}
