package com.labservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// ─────────────────────────────────────────────────────────────
// WHY THIS CLASS EXISTS
// ─────────────────────────────────────────────────────────────
// This is the MASTER CONFIGURATION for security in the entire app.
// It answers THREE questions:
//   1. Which URLs are public (no login needed)?
//   2. Which URLs require which roles?
//   3. How do we check passwords and tokens?
//
// @Configuration = "this class contains Spring configuration"
// @EnableWebSecurity = "turn on Spring Security for this app"
//
// Without this class, Spring Security's defaults kick in:
//   - ALL endpoints require authentication
//   - It generates a random password printed to the console
//   - It uses session-based auth (not JWT)
// We override ALL of that here.
// ─────────────────────────────────────────────────────────────

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // ─────────────────────────────────────────────
    // THE SECURITY RULES
    // ─────────────────────────────────────────────
    // @Bean = "Spring, manage this object. Other classes can use it."
    // SecurityFilterChain = the list of rules for URL access.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── Disable CSRF ──
            // CSRF protection is for browser-based form submissions.
            // We're building a REST API that uses JWT tokens, so CSRF doesn't apply.
            // (JWT tokens already prevent CSRF attacks because they must be
            //  explicitly sent in the Authorization header.)
            .csrf(csrf -> csrf.disable())

            // ── URL Authorization Rules ──
            // This is the core: WHO can access WHAT.
            .authorizeHttpRequests(auth -> auth
                // PUBLIC — no login needed
                .requestMatchers("/api/auth/**").permitAll()        // Login & register
                .requestMatchers("/h2-console/**").permitAll()      // H2 database console
                .requestMatchers("/swagger-ui/**").permitAll()      // Swagger UI pages
                .requestMatchers("/v3/api-docs/**").permitAll()     // OpenAPI JSON spec
                .requestMatchers("/swagger-ui.html").permitAll()    // Swagger entry point

                // PORTAL ACCESS — must have the right role
                .requestMatchers("/api/patient/**").hasAuthority("ROLE_PATIENT")
                .requestMatchers("/api/doctor/**").hasAuthority("ROLE_DOCTOR")
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                // EVERYTHING ELSE — must be logged in (any role)
                .anyRequest().authenticated()
            )

            // ── Session Management ──
            // STATELESS = don't create server-side sessions.
            // Traditional web apps store "who is logged in" on the server.
            // With JWT, the token IS the proof — no server-side state needed.
            // Each request is independent and must include the token.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ── Allow H2 Console to load in iframes ──
            // H2 console uses iframes, which Spring Security blocks by default.
            .headers(headers ->
                headers.frameOptions(frame -> frame.sameOrigin())
            )

            // ── Register our JWT filter ──
            // Tell Spring: "run our JwtAuthenticationFilter BEFORE the default
            // UsernamePasswordAuthenticationFilter"
            // This way, our filter reads the token and sets the authentication
            // before Spring checks the URL rules above.
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ─────────────────────────────────────────────
    // PASSWORD ENCODER
    // ─────────────────────────────────────────────
    // BCrypt is a one-way hash:
    //   "password123" → "$2a$10$xK3jN8vOqR..."
    //
    // You CAN'T reverse it (can't go from hash back to password).
    // To check a login: hash the submitted password and compare hashes.
    //
    // Why not store plain text passwords?
    //   If your database gets hacked, attackers see HASHES, not passwords.
    //   They can't log in with "$2a$10$xK3j..." — only the real password works.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ─────────────────────────────────────────────
    // AUTHENTICATION MANAGER
    // ─────────────────────────────────────────────
    // This is what actually checks "is this username + password correct?"
    // Spring auto-configures it to use:
    //   - Our CustomUserDetailsService (to load the user from the database)
    //   - Our PasswordEncoder (to compare hashed passwords)
    // We expose it as a @Bean so AuthService can use it.
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
