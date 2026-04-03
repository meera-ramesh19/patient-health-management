package com.labservice.dto.request;

// DTO = Data Transfer Object
// It's a simple container that defines the SHAPE of incoming JSON.
// When someone sends POST /api/auth/register, they send JSON like:
//   { "username": "john", "password": "secret", "email": "john@mail.com", "portalType": "patient" }
// Spring automatically converts that JSON into this Java object.

// Why not just use the User entity directly?
// 1. User has fields like "id", "enabled", "role" that the caller should NOT set
// 2. We need "portalType" (patient/doctor) which doesn't exist on User
// 3. DTOs protect your entities — the caller can't sneak in extra fields

public class RegisterRequest {

    private String username;
    private String password;
    private String email;
    private String portalType;   // "patient" or "doctor" — determines which role they get

    public RegisterRequest() {}

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPortalType() { return portalType; }
    public void setPortalType(String portalType) { this.portalType = portalType; }
}
