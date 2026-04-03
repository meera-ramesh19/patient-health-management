package com.labservice.dto.request;

// Shape of forgot password JSON:
//   { "email": "john@mail.com" }
//
// User enters their email → server sends a reset link.
// That's all we need — just the email.

public class ForgotPasswordRequest {

    private String email;

    public ForgotPasswordRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
