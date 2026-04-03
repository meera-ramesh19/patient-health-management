package com.labservice.repository;

import com.labservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // Find user by password reset token
    // Used when user clicks the reset link: /reset-password?token=abc123
    Optional<User> findByResetToken(String resetToken);
}
