package com.labservice.dto.request;

// Shape of reset password JSON:
//   { "token": "abc123-def456", "newPassword": "mynewpassword" }
//
// token       = the reset token from the email link
// newPassword = what the user wants their new password to be
//
// The server checks:
//   1. Is this token valid? (exists in the database)
//   2. Has it expired? (created less than 30 minutes ago)
//   3. If both pass → hash new password → save → clear token

public class ResetPasswordRequest {

    private String token;
    private String newPassword;

    public ResetPasswordRequest() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
