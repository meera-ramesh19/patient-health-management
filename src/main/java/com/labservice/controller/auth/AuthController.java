package com.labservice.controller.auth;

import com.labservice.dto.request.ForgotPasswordRequest;
import com.labservice.dto.request.GoogleLoginRequest;
import com.labservice.dto.request.LoginRequest;
import com.labservice.dto.request.RegisterRequest;
import com.labservice.dto.request.ResetPasswordRequest;
import com.labservice.dto.request.SocialLoginRequest;
import com.labservice.dto.response.LoginResponse;
import com.labservice.model.User;
import com.labservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// ─────────────────────────────────────────────────────────────
// THE AUTH CONTROLLER
// ─────────────────────────────────────────────────────────────
// This is the ONLY controller that's PUBLIC (no login needed).
// SecurityConfig has: .requestMatchers("/api/auth/**").permitAll()
//
// Four endpoints:
//   POST /api/auth/register — create a new account (username + password)
//   POST /api/auth/login    — log in with username + password → JWT token
//   POST /api/auth/google   — log in with Google account → JWT token
//   POST /api/auth/social   — log in with any social provider → JWT token
//
// Both are POST because they SEND data (username, password).
// You never use GET for login because:
//   1. GET parameters appear in the URL (/login?password=secret — visible!)
//   2. GET requests are cached and logged by browsers and servers
//   3. Passwords should ONLY travel in the request body (POST)
// ─────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // POST /api/auth/register
    // Body: { "username": "john", "password": "secret", "email": "john@mail.com", "portalType": "patient" }
    // Returns: the created User (201 Created)
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // POST /api/auth/login
    // Body: { "username": "john", "password": "secret" }
    // Returns: { "token": "eyJhbG...", "username": "john", "role": "ROLE_PATIENT" }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // POST /api/auth/google
    // Body: { "googleToken": "eyJhbG...", "portalType": "patient" }
    // Returns: { "token": "eyJhbG...", "username": "john@gmail.com", "role": "ROLE_PATIENT" }
    //
    // How it works:
    //   1. Frontend shows "Sign in with Google" button
    //   2. User clicks → Google popup → user authenticates with Google
    //   3. Google gives frontend a token (proof of identity)
    //   4. Frontend sends that token HERE
    //   5. Our server asks Google: "Is this token real?"
    //   6. If yes: find or create user → generate OUR JWT token → return it
    //   7. From here on, it works exactly like normal login
    //
    // portalType is only needed on FIRST Google login (when we create the account).
    // On subsequent logins, we already know their role.
    @PostMapping("/google")
    public ResponseEntity<LoginResponse> googleLogin(@RequestBody GoogleLoginRequest request) {
        LoginResponse response = authService.googleLogin(request);
        return ResponseEntity.ok(response);
    }

    // POST /api/auth/social
    // Body: { "provider": "facebook", "accessToken": "EAA...", "portalType": "patient" }
    //    or { "provider": "github",   "accessToken": "gho_...", "portalType": "doctor" }
    //    or { "provider": "google",   "accessToken": "eyJ...", "portalType": "patient" }
    // Returns: { "token": "eyJhbG...", "username": "john@gmail.com", "role": "ROLE_PATIENT" }
    //
    // ONE endpoint for ALL social providers.
    // The "provider" field tells AuthService which provider to verify with.
    // Adding a new provider (Apple, Twitter) = just add verification logic
    // in SocialAuthService. This endpoint doesn't change.
    @PostMapping("/social")
    public ResponseEntity<LoginResponse> socialLogin(@RequestBody SocialLoginRequest request) {
        LoginResponse response = authService.socialLogin(request);
        return ResponseEntity.ok(response);
    }

    // POST /api/auth/forgot-password
    // Body: { "email": "john@mail.com" }
    // Returns: a message (always the same — for security)
    //
    // User says: "I forgot my password"
    // They enter their email → we generate a reset token → save it.
    // In production, we'd email them a link. For now, the token is logged.
    //
    // Why always return 200 OK with the same message?
    // If we returned 404 for unknown emails, an attacker could use this
    // endpoint to discover which emails are registered ("user enumeration").
    // By always saying "if that email is registered, a link was sent",
    // attackers learn nothing.
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String message = authService.forgotPassword(request);
        return ResponseEntity.ok(message);
    }

    // POST /api/auth/reset-password
    // Body: { "token": "a1b2c3d4-e5f6-...", "newPassword": "mynewpassword" }
    // Returns: success message or error
    //
    // User clicked the reset link in their email → entered their new password.
    // We verify the token, check expiry, hash the new password, and save it.
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        String message = authService.resetPassword(request);
        return ResponseEntity.ok(message);
    }
}
