package com.labservice.service;

import com.labservice.exception.ResourceNotFoundException;
import com.labservice.model.Role;
import com.labservice.model.User;
import com.labservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Get all users (Admin Portal)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get one user by ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    // Get user by username (used during login)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    // Enable a user account (Admin approves a doctor registration)
    public User enableUser(Long id) {
        User user = getUserById(id);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    // Disable a user account (Admin suspends an account)
    public User disableUser(Long id) {
        User user = getUserById(id);
        user.setEnabled(false);
        return userRepository.save(user);
    }

    // Change a user's role (Admin promotes/demotes)
    public User changeRole(Long id, Role newRole) {
        User user = getUserById(id);
        user.setRole(newRole);
        return userRepository.save(user);
    }

    // Delete a user account
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
