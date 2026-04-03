package com.labservice.exception;

// Thrown when the user sends invalid data or breaks a business rule.
// Examples:
//   "Cannot book appointments in the past"
//   "End date cannot be before start date"
//   "Doctor already has an appointment at this time"
//   "Cannot change status of a COMPLETED lab order"
//
// This maps to HTTP 400 Bad Request.
// The GlobalExceptionHandler catches this and returns 400 automatically.

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
