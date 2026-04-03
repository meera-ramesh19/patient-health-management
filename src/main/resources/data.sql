-- ============================================================
-- SEED DATA — runs automatically on every startup
-- ============================================================
-- Creates a default admin account so you can log in immediately.
--
-- Username: admin
-- Password: admin123 (BCrypt hashed below)
-- Role: ROLE_ADMIN
--
-- The password hash was generated with BCryptPasswordEncoder.
-- You can NEVER reverse "$2a$10$..." back to "admin123" — that's the point!
--
-- To generate a new hash, you can use an online BCrypt generator
-- or run: new BCryptPasswordEncoder().encode("yourpassword") in Java.

INSERT INTO users (username, password, email, role, enabled) VALUES
('admin', '$2b$10$HE0WP8RkKjnagL02/CDvFuWIS7aT9b/LIJd2.dqL9QjVDH0KpyPwm', 'admin@labservice.com', 'ROLE_ADMIN', true)
ON CONFLICT (username) DO NOTHING;
