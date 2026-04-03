package com.labservice.exception;

// Thrown when trying to create something that already exists.
// Examples:
//   "Username already taken: john"
//   "Email already registered: john@mail.com"
//
// This maps to HTTP 409 Conflict.
// The GlobalExceptionHandler catches this and returns 409 automatically.

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
