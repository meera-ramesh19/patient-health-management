package com.labservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// ─────────────────────────────────────────────────────────────
// WHY THIS CLASS EXISTS
// ─────────────────────────────────────────────────────────────
// Sends emails from the application. Currently used for:
//   - Password reset links (forgot password flow)
//
// HOW IT WORKS:
//   JavaMailSender is Spring's email-sending tool.
//   Spring auto-configures it from application.properties:
//     spring.mail.host = SMTP server (e.g., smtp.gmail.com)
//     spring.mail.port = port number (587 for TLS)
//     spring.mail.username = your email address
//     spring.mail.password = your app password
//
//   SimpleMailMessage = a plain text email (no HTML, no attachments).
//   For HTML emails, you'd use MimeMessage + MimeMessageHelper instead.
//
// WHY A SEPARATE SERVICE?
//   - AuthService shouldn't know HOW to send emails (separation of concerns)
//   - Easy to swap email provider later (Gmail → SendGrid → AWS SES)
//   - Easy to mock in tests (just mock EmailService, don't send real emails)
// ─────────────────────────────────────────────────────────────

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Read from application.properties
    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // ─────────────────────────────────────────────
    // SEND PASSWORD RESET EMAIL
    // ─────────────────────────────────────────────
    // Called from AuthService.forgotPassword()
    //
    // Sends an email like:
    //   To: john@mail.com
    //   Subject: LabService — Reset Your Password
    //   Body: Click the link below to reset your password:
    //         http://localhost:3000/reset-password?token=abc123-def456
    //         This link expires in 30 minutes.
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("LabService — Reset Your Password");
        message.setText(
                "You requested a password reset for your LabService account.\n\n"
                + "Click the link below to reset your password:\n"
                + frontendUrl + "/reset-password?token=" + resetToken + "\n\n"
                + "This link expires in 30 minutes.\n\n"
                + "If you did not request this, please ignore this email.\n"
                + "Your password will remain unchanged."
        );

        try {
            mailSender.send(message);
        } catch (Exception e) {
            // Log the error but don't crash the app
            // The user still gets the "check your email" response
            // even if email delivery fails
            System.err.println("Failed to send password reset email to " + toEmail + ": " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    // SEND GENERIC EMAIL
    // ─────────────────────────────────────────────
    // Utility method for sending any plain-text email.
    // Can be used for future features like:
    //   - Appointment reminders
    //   - Lab results ready notifications
    //   - Welcome emails after registration
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }
}
