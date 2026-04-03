package com.labservice.exception;

// Thrown when a user tries to access something they don't own.
// Examples:
//   "You can only cancel your own appointments"
//   "Invalid reset token"
//   "Invalid Google token"
//
// This maps to HTTP 401 Unauthorized.
// The GlobalExceptionHandler catches this and returns 401 automatically.

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
