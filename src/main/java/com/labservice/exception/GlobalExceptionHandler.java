package com.labservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// ─────────────────────────────────────────────────────────────
// THE GLOBAL EXCEPTION HANDLER
// ─────────────────────────────────────────────────────────────
// This class catches exceptions thrown ANYWHERE in the app
// and converts them into clean JSON error responses.
//
// WITHOUT this class:
//   - Exception → Spring returns ugly 500 error with stack trace
//   - Frontend gets a messy HTML page or raw error text
//
// WITH this class:
//   - Exception → caught here → clean JSON with proper HTTP status
//   - Frontend always gets: { status, error, message, timestamp }
//
// HOW IT WORKS:
//   @ControllerAdvice = "Apply this to ALL controllers"
//   @ExceptionHandler = "When THIS exception is thrown, run THIS method"
//
//   Think of it as a safety net under a trapeze.
//   No matter which controller throws an exception,
//   it falls down here and gets caught.
//
// EXAMPLE:
//   Service throws: new ResourceNotFoundException("Patient not found with id: 5")
//   This class catches it and returns:
//     HTTP 404
//     { "status": 404, "error": "Not Found", "message": "Patient not found with id: 5", "timestamp": "..." }
// ─────────────────────────────────────────────────────────────

@ControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 NOT FOUND ──
    // Catches: ResourceNotFoundException
    // When: looking up something that doesn't exist in the database
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),        // 404
                "Not Found",
                ex.getMessage()                      // "Patient not found with id: 5"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // ── 400 BAD REQUEST ──
    // Catches: BadRequestException
    // When: user sends invalid data or breaks a business rule
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),       // 400
                "Bad Request",
                ex.getMessage()                       // "Cannot book appointments in the past"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ── 409 CONFLICT ──
    // Catches: DuplicateResourceException
    // When: trying to create something that already exists
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),          // 409
                "Conflict",
                ex.getMessage()                       // "Username already taken: john"
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // ── 401 UNAUTHORIZED ──
    // Catches: UnauthorizedException
    // When: user tries to access something they don't own, or invalid auth token
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),      // 401
                "Unauthorized",
                ex.getMessage()                       // "You can only cancel your own appointments"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // ── 500 INTERNAL SERVER ERROR ──
    // Catches: any other Exception we didn't specifically handle
    // This is the "catch-all" — if something unexpected happens,
    // the user still gets a clean JSON response instead of a stack trace.
    //
    // We DON'T expose the real error message to the user (could leak
    // database details, file paths, etc.). Instead, we log it and
    // return a generic message.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        // Log the real error for developers to debug
        ex.printStackTrace();

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),   // 500
                "Internal Server Error",
                "Something went wrong. Please try again later."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
