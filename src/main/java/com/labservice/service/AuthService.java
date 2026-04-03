package com.labservice.service;

import com.labservice.exception.BadRequestException;
import com.labservice.exception.DuplicateResourceException;
import com.labservice.exception.ResourceNotFoundException;
import com.labservice.exception.UnauthorizedException;
import com.labservice.dto.request.GoogleLoginRequest;
import com.labservice.dto.request.LoginRequest;
import com.labservice.dto.request.RegisterRequest;
import com.labservice.dto.request.ForgotPasswordRequest;
import com.labservice.dto.request.ResetPasswordRequest;
import com.labservice.dto.request.SocialLoginRequest;
import com.labservice.dto.response.LoginResponse;
import com.labservice.model.Patient;
import com.labservice.model.Role;
import com.labservice.model.User;
import com.labservice.repository.UserRepository;
import com.labservice.security.JwtTokenProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

// ─────────────────────────────────────────────────────────────
// WHY THIS CLASS EXISTS
// ─────────────────────────────────────────────────────────────
// Handles the public operations: REGISTER, LOGIN, GOOGLE LOGIN, and SOCIAL LOGIN.
//
// Register flow:
//   1. Check if username/email already taken
//   2. Hash the password (never store plain text!)
//   3. Assign role based on portalType ("patient" → ROLE_PATIENT)
//   4. Save user to database
//   5. If patient, auto-create an empty Patient profile
//
// Login flow:
//   1. Check username + password against database
//   2. If correct, generate a JWT token
//   3. Return token + username + role
// ─────────────────────────────────────────────────────────────

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientService patientService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private SocialAuthService socialAuthService;

    @Autowired
    private EmailService emailService;

    // Google Client ID from application.properties
    @Value("${google.client-id}")
    private String googleClientId;

    // ─────────────────────────────────────────────
    // REGISTER A NEW USER
    // ─────────────────────────────────────────────
    public User register(RegisterRequest request) {

        // Rule 1: Username must be unique
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }

        // Rule 2: Email must be unique
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        // Create the User entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        // CRITICAL: Hash the password before saving!
        // "password123" → "$2a$10$xK3jN8vOqR..."
        // The database NEVER sees the plain text password.
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign role based on portal type
        // "patient" → ROLE_PATIENT, "doctor" → ROLE_DOCTOR
        if ("doctor".equalsIgnoreCase(request.getPortalType())) {
            user.setRole(Role.ROLE_DOCTOR);
            user.setEnabled(false);   // Doctor accounts need admin approval
        } else {
            user.setRole(Role.ROLE_PATIENT);
            user.setEnabled(true);    // Patient accounts are active immediately
        }

        // Save user to database
        User savedUser = userRepository.save(user);

        // If registering as a patient, auto-create an empty Patient profile
        // This way, the patient can immediately update their profile after login
        if (savedUser.getRole() == Role.ROLE_PATIENT) {
            Patient patient = new Patient();
            patient.setUser(savedUser);
            patient.setFirstName("");
            patient.setLastName("");
            patient.setEmail(savedUser.getEmail());
            patientService.createPatient(patient);
        }

        return savedUser;
    }

    // ─────────────────────────────────────────────
    // LOGIN AN EXISTING USER
    // ─────────────────────────────────────────────
    public LoginResponse login(LoginRequest request) {

        // AuthenticationManager checks:
        //   1. Does this username exist? (via CustomUserDetailsService)
        //   2. Does the password match? (via PasswordEncoder)
        // If either fails, it throws an exception → 401 Unauthorized
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // If we reach here, credentials are valid.
        // Load the user to get their role.
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Generate a JWT token containing username + role
        String token = jwtTokenProvider.generateToken(
                user.getUsername(),
                user.getRole().name()     // "ROLE_PATIENT" as a string
        );

        // Return the token + user info
        return new LoginResponse(token, user.getUsername(), user.getRole().name());
    }

    // ─────────────────────────────────────────────
    // LOGIN WITH GOOGLE
    // ─────────────────────────────────────────────
    // This handles "Sign in with Google" from the frontend.
    //
    // Flow:
    //   1. Frontend shows Google Sign-In button
    //   2. User clicks → Google popup → user logs into Google
    //   3. Google gives frontend an "ID Token" (proof of identity)
    //   4. Frontend sends that token to POST /api/auth/google
    //   5. We verify the token with Google (is it real? not expired?)
    //   6. Google gives us: email + name
    //   7. If email exists in our database → log them in
    //      If email is new → create account automatically
    //   8. Generate our JWT token (same as normal login)
    //
    // The user NEVER types a password — Google handles that part.
    public LoginResponse googleLogin(GoogleLoginRequest request) {

        // ── Step 1: Verify the Google token ──
        // GoogleIdTokenVerifier asks Google: "Is this token real?"
        // It checks:
        //   - Was this token signed by Google? (not forged)
        //   - Is it for OUR app? (matches our Client ID)
        //   - Has it expired?
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))  // Must match our Client ID
                .build();

        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(request.getGoogleToken());
        } catch (Exception e) {
            throw new UnauthorizedException("Failed to verify Google token");
        }

        if (idToken == null) {
            throw new UnauthorizedException("Invalid Google token");
        }

        // ── Step 2: Extract user info from the verified token ──
        // Google guarantees this info is correct (they verified the user)
        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();                              // "john@gmail.com"
        String name = (String) payload.get("name");                     // "John Smith"
        String firstName = (String) payload.get("given_name");          // "John"
        String lastName = (String) payload.get("family_name");          // "Smith"

        // ── Step 3: Find or create the user ──
        // If this email already exists → log them in
        // If this email is new → create an account automatically
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            // Existing user — just log them in
            user = existingUser.get();
        } else {
            // New user — create account automatically
            user = new User();
            user.setEmail(email);
            user.setUsername(email);   // Use email as username for Google users

            // Google users don't have a password in our system.
            // Set a random password they'll never use — BCrypt hashed for safety.
            // They'll always log in via Google, never with this password.
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

            // Assign role based on portal type (defaults to patient)
            String portalType = request.getPortalType();
            if ("doctor".equalsIgnoreCase(portalType)) {
                user.setRole(Role.ROLE_DOCTOR);
                user.setEnabled(false);   // Doctors need admin approval
            } else {
                user.setRole(Role.ROLE_PATIENT);
                user.setEnabled(true);
            }

            user = userRepository.save(user);

            // If patient, auto-create Patient profile with Google name
            if (user.getRole() == Role.ROLE_PATIENT) {
                Patient patient = new Patient();
                patient.setUser(user);
                patient.setFirstName(firstName != null ? firstName : "");
                patient.setLastName(lastName != null ? lastName : "");
                patient.setEmail(email);
                patientService.createPatient(patient);
            }
        }

        // ── Step 4: Generate OUR JWT token ──
        // From here on, it's identical to normal login.
        // The frontend uses our JWT token, not Google's token.
        String token = jwtTokenProvider.generateToken(
                user.getUsername(),
                user.getRole().name()
        );

        return new LoginResponse(token, user.getUsername(), user.getRole().name());
    }

    // ─────────────────────────────────────────────
    // UNIFIED SOCIAL LOGIN (Google, Facebook, GitHub)
    // ─────────────────────────────────────────────
    // One method handles ALL social providers.
    // The SocialAuthService knows how to verify each provider's token.
    // After verification, the find-or-create logic is IDENTICAL
    // regardless of which provider the user logged in with.
    //
    //   POST /api/auth/social
    //   { "provider": "facebook", "accessToken": "EAA...", "portalType": "patient" }
    //
    // This replaces having separate /google, /facebook, /github endpoints.
    // Adding a new provider (e.g., Apple, Twitter) = just add verification
    // logic in SocialAuthService. This method doesn't change.
    public LoginResponse socialLogin(SocialLoginRequest request) {

        // ── Step 1: Verify token with the provider ──
        // SocialAuthService routes to the right provider:
        //   "google"   → calls Google's verifier
        //   "facebook" → calls Facebook's Graph API
        //   "github"   → calls GitHub's API
        // Returns: SocialUserInfo { email, firstName, lastName }
        SocialUserInfo userInfo = socialAuthService.verifyAndGetUserInfo(
                request.getProvider(),
                request.getAccessToken()
        );

        // ── Step 2: Find or create user (same for ALL providers) ──
        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());
        User user;

        if (existingUser.isPresent()) {
            // Returning user — just log them in
            user = existingUser.get();
        } else {
            // New user — create account automatically
            user = new User();
            user.setEmail(userInfo.getEmail());
            user.setUsername(userInfo.getEmail());   // Use email as username
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

            String portalType = request.getPortalType();
            if ("doctor".equalsIgnoreCase(portalType)) {
                user.setRole(Role.ROLE_DOCTOR);
                user.setEnabled(false);
            } else {
                user.setRole(Role.ROLE_PATIENT);
                user.setEnabled(true);
            }

            user = userRepository.save(user);

            if (user.getRole() == Role.ROLE_PATIENT) {
                Patient patient = new Patient();
                patient.setUser(user);
                patient.setFirstName(userInfo.getFirstName() != null ? userInfo.getFirstName() : "");
                patient.setLastName(userInfo.getLastName() != null ? userInfo.getLastName() : "");
                patient.setEmail(userInfo.getEmail());
                patientService.createPatient(patient);
            }
        }

        // ── Step 3: Generate OUR JWT token ──
        String token = jwtTokenProvider.generateToken(
                user.getUsername(),
                user.getRole().name()
        );

        return new LoginResponse(token, user.getUsername(), user.getRole().name());
    }

    // ─────────────────────────────────────────────
    // FORGOT PASSWORD
    // ─────────────────────────────────────────────
    // User says: "I forgot my password"
    // They give us their email → we generate a random reset token → save it.
    //
    // Flow:
    //   1. User enters their email on the "Forgot Password" page
    //   2. Frontend sends POST /api/auth/forgot-password { "email": "john@mail.com" }
    //   3. We look up the user by email
    //   4. Generate a random token (UUID = a random string like "a1b2c3d4-e5f6-...")
    //   5. Save the token + expiry time (30 minutes from now) to the user record
    //   6. In a real app, we'd EMAIL this token as a link:
    //      https://yourapp.com/reset-password?token=a1b2c3d4-e5f6-...
    //      For now, we return the token in the response (for testing).
    //
    // SECURITY NOTE: We always return "success" even if the email doesn't exist.
    // Why? If we said "email not found", an attacker could figure out which
    // emails are registered in our system. This is called "user enumeration".
    public String forgotPassword(ForgotPasswordRequest request) {

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // Generate a random token — UUID gives us a unique string every time
            // Example: "550e8400-e29b-41d4-a716-446655440000"
            String token = UUID.randomUUID().toString();

            // Save the token and set it to expire in 30 minutes
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
            userRepository.save(user);

            // Send the password reset email with the token link
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        }

        // Always return the same message (don't reveal if email exists or not)
        return "If that email is registered, a reset link has been sent.";
    }

    // ─────────────────────────────────────────────
    // RESET PASSWORD
    // ─────────────────────────────────────────────
    // User clicked the reset link in their email → lands on a page where
    // they enter their new password.
    //
    // Flow:
    //   1. Frontend sends POST /api/auth/reset-password
    //      { "token": "a1b2c3d4-...", "newPassword": "mynewpassword" }
    //   2. We find the user who has this token
    //   3. Check if the token has expired (must be within 30 minutes)
    //   4. Hash the new password → save it
    //   5. Clear the token (so it can't be reused)
    //
    // Three things can go wrong:
    //   - Token doesn't exist (invalid link)
    //   - Token has expired (too slow, past 30 minutes)
    //   - Everything works → password is updated!
    public String resetPassword(ResetPasswordRequest request) {

        // Step 1: Find the user who owns this token
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid reset token"));

        // Step 2: Check if the token has expired
        // LocalDateTime.now() = right now
        // resetTokenExpiry = when the token was supposed to expire
        // If right now is AFTER the expiry → token is expired
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired. Please request a new one.");
        }

        // Step 3: Hash the new password and save it
        // Just like during registration — NEVER store plain text passwords
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // Step 4: Clear the reset token (one-time use only!)
        // If we didn't clear it, someone could reuse the link to reset again
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        // Save the updated user
        userRepository.save(user);

        return "Password has been reset successfully.";
    }
}
