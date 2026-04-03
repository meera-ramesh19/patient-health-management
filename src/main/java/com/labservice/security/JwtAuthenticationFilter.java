package com.labservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

// ─────────────────────────────────────────────────────────────
// WHY THIS CLASS EXISTS
// ─────────────────────────────────────────────────────────────
// This filter runs BEFORE every HTTP request reaches your controllers.
//
// The flow:
//   HTTP Request → [JwtAuthenticationFilter] → SecurityConfig → Controller
//
// What it does:
//   1. Look for the "Authorization: Bearer <token>" header
//   2. If found, validate the token
//   3. If valid, tell Spring Security "this user is authenticated"
//   4. If missing or invalid, do nothing (Spring Security will reject if needed)
//
// Think of it as a BOUNCER at a club:
//   - Checks your ID (token) at the door
//   - If valid ID, lets you in and stamps your hand (sets authentication)
//   - If no ID, doesn't let you in (unless the club has free entry areas like /api/auth)
//
// "OncePerRequestFilter" = this filter runs exactly ONCE per request
//   (not multiple times if the request gets forwarded internally)
// ─────────────────────────────────────────────────────────────

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ── Step 1: Get the token from the request header ──
        // The header looks like: "Authorization: Bearer eyJhbGciOi..."
        // We need to strip "Bearer " to get just the token.
        String token = getTokenFromRequest(request);

        // ── Step 2: If token exists AND is valid, authenticate the user ──
        if (token != null && jwtTokenProvider.validateToken(token)) {

            // Read username and role from inside the token
            String username = jwtTokenProvider.getUsernameFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            // Create an "authentication object" — Spring Security's way of saying
            // "this user is who they claim to be, and they have this role"
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,                                           // principal (who)
                            null,                                               // credentials (password — not needed, token already validated)
                            Collections.singletonList(
                                    new SimpleGrantedAuthority(role)             // authorities (what roles)
                            )
                    );

            // Attach request details (IP address, session ID, etc.)
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // ── Step 3: Tell Spring Security "this user is authenticated" ──
            // SecurityContextHolder is like a global variable that holds the current user.
            // After this line, any code can call SecurityContextHolder.getContext()
            // to find out who is making the request.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // ── Step 4: Continue to the next filter / controller ──
        // Whether we authenticated or not, pass the request along.
        // If we didn't authenticate and the URL requires a role, Spring Security
        // will return 401/403 automatically.
        filterChain.doFilter(request, response);
    }

    // ─────────────────────────────────────────────
    // HELPER: Extract token from "Authorization: Bearer <token>"
    // ─────────────────────────────────────────────
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Check if header exists and starts with "Bearer "
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);   // Remove "Bearer " prefix (7 characters)
        }
        return null;  // No token found
    }
}
