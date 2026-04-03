package com.labservice.dto.response;

// Shape of what the server sends BACK after a successful login:
//   { "token": "eyJhbG...", "username": "john", "role": "ROLE_PATIENT" }
//
// The frontend uses:
//   - token    → saves it, sends it with every future request
//   - username → displays "Welcome, john!"
//   - role     → redirects to the correct portal (patient/doctor/admin)

public class LoginResponse {

    private String token;
    private String username;
    private String role;

    // Constructor with all fields (so we can create it in one line)
    public LoginResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    // Getters (no setters needed — we set everything in the constructor)
    public String getToken() { return token; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}
