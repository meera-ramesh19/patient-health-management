package com.labservice.service;

import com.labservice.dto.request.ForgotPasswordRequest;
import com.labservice.dto.request.RegisterRequest;
import com.labservice.dto.request.ResetPasswordRequest;
import com.labservice.exception.BadRequestException;
import com.labservice.exception.DuplicateResourceException;
import com.labservice.exception.UnauthorizedException;
import com.labservice.model.Role;
import com.labservice.model.User;
import com.labservice.repository.UserRepository;
import com.labservice.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// ─────────────────────────────────────────────────────────────
// UNIT TEST FOR AuthService
// ─────────────────────────────────────────────────────────────
// Tests: registration validation, forgot/reset password logic
// ─────────────────────────────────────────────────────────────

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PatientService patientService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private SocialAuthService socialAuthService;
    @Mock private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@mail.com");
        registerRequest.setPassword("password123");
        registerRequest.setPortalType("patient");
    }

    @Test
    void register_validData_createsUser() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        User result = authService.register(registerRequest);

        assertEquals("newuser", result.getUsername());
        assertEquals(Role.ROLE_PATIENT, result.getRole());
        assertTrue(result.isEnabled());
        verify(patientService).createPatient(any());
    }

    @Test
    void register_duplicateUsername_throwsDuplicateResource() {
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> authService.register(registerRequest));
    }

    @Test
    void register_duplicateEmail_throwsDuplicateResource() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@mail.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> authService.register(registerRequest));
    }

    @Test
    void register_doctorPortalType_setsRoleDoctorAndDisabled() {
        registerRequest.setPortalType("doctor");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = authService.register(registerRequest);

        assertEquals(Role.ROLE_DOCTOR, result.getRole());
        assertFalse(result.isEnabled());
    }

    @Test
    void forgotPassword_existingEmail_generatesToken() {
        User user = new User();
        user.setEmail("john@mail.com");
        when(userRepository.findByEmail("john@mail.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("john@mail.com");

        String result = authService.forgotPassword(request);

        assertNotNull(user.getResetToken());
        assertNotNull(user.getResetTokenExpiry());
        verify(emailService).sendPasswordResetEmail(eq("john@mail.com"), anyString());
        assertEquals("If that email is registered, a reset link has been sent.", result);
    }

    @Test
    void forgotPassword_unknownEmail_returnsSameMessage() {
        when(userRepository.findByEmail("unknown@mail.com")).thenReturn(Optional.empty());

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("unknown@mail.com");

        String result = authService.forgotPassword(request);

        assertEquals("If that email is registered, a reset link has been sent.", result);
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void resetPassword_validToken_updatesPassword() {
        User user = new User();
        user.setResetToken("valid-token");
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));

        when(userRepository.findByResetToken("valid-token")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpassword")).thenReturn("hashedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("valid-token");
        request.setNewPassword("newpassword");

        String result = authService.resetPassword(request);

        assertEquals("Password has been reset successfully.", result);
        assertEquals("hashedNewPassword", user.getPassword());
        assertNull(user.getResetToken());
        assertNull(user.getResetTokenExpiry());
    }

    @Test
    void resetPassword_expiredToken_throwsBadRequest() {
        User user = new User();
        user.setResetToken("expired-token");
        user.setResetTokenExpiry(LocalDateTime.now().minusMinutes(5));

        when(userRepository.findByResetToken("expired-token")).thenReturn(Optional.of(user));

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("expired-token");
        request.setNewPassword("newpassword");

        assertThrows(BadRequestException.class,
                () -> authService.resetPassword(request));
    }

    @Test
    void resetPassword_invalidToken_throwsUnauthorized() {
        when(userRepository.findByResetToken("bad-token")).thenReturn(Optional.empty());

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken("bad-token");
        request.setNewPassword("newpassword");

        assertThrows(UnauthorizedException.class,
                () -> authService.resetPassword(request));
    }
}
