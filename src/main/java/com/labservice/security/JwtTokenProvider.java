package com.labservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

// @Component = like @Service but more generic.
// Tells Spring: "manage this class, make it injectable with @Autowired"
// We use @Component instead of @Service because this isn't business logic —
// it's a utility that creates/reads tokens.

@Component
public class JwtTokenProvider {

    // @Value reads from application.properties
    // jwt.secret=my-super-secret-key-for-labservice-change-in-production-1234567890
    // This key is used to SIGN the token — like a wax seal on a letter.
    // If someone tampers with the token, the signature won't match and we reject it.
    @Value("${jwt.secret}")
    private String jwtSecret;

    // jwt.expiration=86400000 (24 hours in milliseconds)
    // After this time, the token expires and the user must log in again.
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // ─────────────────────────────────────────────
    // GET THE SIGNING KEY
    // ─────────────────────────────────────────────
    // Converts the secret string into a cryptographic Key object.
    // Think of it as turning your password into a proper wax seal stamp.
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // ─────────────────────────────────────────────
    // GENERATE A TOKEN (called after successful login)
    // ─────────────────────────────────────────────
    // Input: username + role
    // Output: a signed JWT string like "eyJhbGciOiJIUzI1NiJ9.eyJ..."
    //
    // The token contains:
    //   - subject = username ("john")
    //   - claim "role" = "ROLE_PATIENT"
    //   - issuedAt = now
    //   - expiration = now + 24 hours
    //   - signature = signed with our secret key
    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(username)                    // WHO this token is for
                .claim("role", role)                  // WHAT role they have
                .issuedAt(now)                        // WHEN it was created
                .expiration(expiryDate)               // WHEN it expires
                .signWith(getSigningKey())             // SIGN it with our secret
                .compact();                            // BUILD the string
    }

    // ─────────────────────────────────────────────
    // READ THE USERNAME FROM A TOKEN
    // ─────────────────────────────────────────────
    // Input: the JWT string from the Authorization header
    // Output: the username stored inside ("john")
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())         // Use same key to verify
                .build()
                .parseSignedClaims(token)               // Parse and verify signature
                .getPayload()
                .getSubject();                          // Extract the username
    }

    // ─────────────────────────────────────────────
    // READ THE ROLE FROM A TOKEN
    // ─────────────────────────────────────────────
    // Input: the JWT string
    // Output: the role stored inside ("ROLE_PATIENT")
    public String getRoleFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);             // Extract the "role" claim
    }

    // ─────────────────────────────────────────────
    // VALIDATE A TOKEN
    // ─────────────────────────────────────────────
    // Checks:
    //   1. Is the signature valid? (was it signed with OUR secret?)
    //   2. Has it expired?
    //   3. Is it properly formatted?
    // Returns true if valid, false if anything is wrong.
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);          // This throws if invalid
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Token is invalid — expired, tampered, malformed, etc.
            return false;
        }
    }
}
