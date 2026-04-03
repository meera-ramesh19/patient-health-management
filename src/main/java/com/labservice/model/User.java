package com.labservice.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity                         // Mark as database table
@Table(name = "users")          // Name the table
public class User {
                      
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) // Primary key // Auto-increment
    private Long id;

    @Column(nullable = false, unique=true)  // Cannot be null and must be unique
    private String username;

    @Column(nullable = false)     // Cannot be null in the database
    private String password;
      
    @Column(nullable = false,unique=true)  // Cannot be null and must be unique
    private String email;

    @Enumerated(EnumType.STRING)    // Stores "ROLE_PATIENT" as text, not a number
    @Column(nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user")
    private Patient patient;

    @OneToOne(mappedBy = "user")
    private Doctor doctor;

    private boolean enabled;        // Admin can disable accounts without deleting them

    // Password reset fields
    // When user clicks "Forgot Password":
    //   - resetToken = random string sent via email (like a temporary key)
    //   - resetTokenExpiry = when the token expires (30 minutes from creation)
    // After password is reset, both are set back to null.
    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    // Empty constructor (JPA REQUIRES this)
    public User() {}

    // Getters and Setters for EVERY field
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { this.resetTokenExpiry = resetTokenExpiry; }
}