package com.labservice.security;

import com.labservice.model.User;
import com.labservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

// ─────────────────────────────────────────────────────────────
// WHY THIS CLASS EXISTS
// ─────────────────────────────────────────────────────────────
// Spring Security doesn't know about YOUR User entity or YOUR database.
// It only knows about its own "UserDetails" interface.
//
// This class is the BRIDGE:
//   Your database (User entity) ──→ Spring Security (UserDetails interface)
//
// Spring Security calls loadUserByUsername("john") and expects back
// a UserDetails object containing: username, password, and roles.
// We load OUR User from the database and convert it.
//
// Think of it like a translator:
//   Spring Security speaks "UserDetails"
//   Our database speaks "User"
//   This class translates between them.
// ─────────────────────────────────────────────────────────────

@Service
public class CustomUserDetailsService implements UserDetailsService {

    // "implements UserDetailsService" = promise to provide loadUserByUsername()

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Step 1: Find OUR User in the database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Step 2: Convert OUR User → Spring Security's UserDetails
        // SimpleGrantedAuthority wraps "ROLE_PATIENT" into a format Spring understands
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),                                              // username
                user.getPassword(),                                             // hashed password
                user.isEnabled(),                                               // is account active?
                true,                                                           // account not expired
                true,                                                           // credentials not expired
                true,                                                           // account not locked
                Collections.singletonList(                                      // list of roles
                        new SimpleGrantedAuthority(user.getRole().name())        // "ROLE_PATIENT"
                )
        );
    }
}
