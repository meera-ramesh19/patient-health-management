package com.labservice.exception;

import java.time.LocalDateTime;

// The JSON shape that ALL errors return.
// Instead of a raw string or a messy stack trace, the frontend
// always gets a clean, consistent error object:
//
//   {
//     "status": 404,
//     "error": "Not Found",
//     "message": "Patient not found with id: 5",
//     "timestamp": "2026-03-15T14:30:00"
//   }
//
// This makes it easy for the frontend to show error messages.
// Every field is predictable — the frontend can always do:
//   alert(response.data.message)

public class ErrorResponse {

    private int status;              // HTTP status code (404, 400, 409, 401, 500)
    private String error;            // Short label ("Not Found", "Bad Request")
    private String message;          // Detailed message ("Patient not found with id: 5")
    private LocalDateTime timestamp; // When the error happened

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Getters (Jackson needs these to convert to JSON)
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
