package com.labservice.exception;

// Thrown when something doesn't exist in the database.
// Examples:
//   "Patient not found with id: 5"
//   "Doctor not found with id: 12"
//   "Appointment not found with id: 99"
//
// This maps to HTTP 404 Not Found.
// The GlobalExceptionHandler catches this and returns 404 automatically.

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
