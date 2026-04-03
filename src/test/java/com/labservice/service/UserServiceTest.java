package com.labservice.service;

import com.labservice.exception.ResourceNotFoundException;
import com.labservice.model.Role;
import com.labservice.model.User;
import com.labservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// ─────────────────────────────────────────────────────────────
// UNIT TEST FOR UserService
// ─────────────────────────────────────────────────────────────
// Tests the business logic in UserService WITHOUT hitting a real database.
//
// Key concepts:
//   @ExtendWith(MockitoExtension.class) — enables Mockito (mocking framework)
//   @Mock — creates a FAKE version of a class (doesn't hit the database)
//   @InjectMocks — creates the real service but injects the fakes into it
//   @Test — marks a method as a test case
//   @BeforeEach — runs before EVERY test (sets up test data)
//
// WHY MOCK?
//   We're testing UserService, NOT the database.
//   If the database is broken, we don't want UserService tests to fail.
//   Mocks let us control exactly what the "database" returns.
// ─────────────────────────────────────────────────────────────

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("john");
        testUser.setEmail("john@mail.com");
        testUser.setPassword("hashedPassword");
        testUser.setRole(Role.ROLE_PATIENT);
        testUser.setEnabled(true);
    }

    @Test
    void getAllUsers_returnsAllUsers() {
        // Arrange: tell the mock what to return
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("jane");
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        // Act: call the real method
        List<User> result = userService.getAllUsers();

        // Assert: check the result
        assertEquals(2, result.size());
        assertEquals("john", result.get(0).getUsername());
    }

    @Test
    void getUserById_existingId_returnsUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.getUserById(1L);

        assertEquals("john", result.getUsername());
        assertEquals("john@mail.com", result.getEmail());
    }

    @Test
    void getUserById_nonExistingId_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(99L));
    }

    @Test
    void getUserByUsername_existingUsername_returnsUser() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(testUser));

        User result = userService.getUserByUsername("john");

        assertEquals(1L, result.getId());
    }

    @Test
    void getUserByUsername_nonExisting_throwsException() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserByUsername("nobody"));
    }

    @Test
    void enableUser_setsEnabledTrue() {
        testUser.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.enableUser(1L);

        assertTrue(result.isEnabled());
        verify(userRepository).save(testUser);
    }

    @Test
    void disableUser_setsEnabledFalse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.disableUser(1L);

        assertFalse(result.isEnabled());
    }

    @Test
    void changeRole_updatesRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.changeRole(1L, Role.ROLE_DOCTOR);

        assertEquals(Role.ROLE_DOCTOR, result.getRole());
    }

    @Test
    void deleteUser_callsDeleteById() {
        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }
}
