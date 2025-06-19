package com.padudjayaputera.sistem_akuntansi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.padudjayaputera.sistem_akuntansi.dto.RegisterRequest;
import com.padudjayaputera.sistem_akuntansi.model.Division;
import com.padudjayaputera.sistem_akuntansi.model.User;
import com.padudjayaputera.sistem_akuntansi.service.DivisionService;
import com.padudjayaputera.sistem_akuntansi.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final DivisionService divisionService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, DivisionService divisionService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.divisionService = divisionService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get all users
     */
    @GetMapping
    // @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            System.out.println("Found " + users.size() + " users");
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.err.println("Error getting users: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Create new user using RegisterRequest DTO
     */
    @PostMapping
    // @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody RegisterRequest request) {
        try {
            System.out.println("=== DEBUG: Received RegisterRequest ===");
            System.out.println("Username: " + request.getUsername());
            System.out.println("Password: " + (request.getPassword() != null ? "[HIDDEN - LENGTH: " + request.getPassword().length() + "]" : "NULL"));
            System.out.println("Role: " + request.getRole());
            System.out.println("Division ID: " + request.getDivisionId());
            System.out.println("======================================");
            
            // Validate required fields
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Username is required");
            }
            
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                System.err.println("Password is null or empty!");
                return ResponseEntity.badRequest().body("Password is required");
            }
            
            if (request.getRole() == null) {
                return ResponseEntity.badRequest().body("Role is required");
            }
            
            // Check if username already exists
            try {
                userService.findByUsername(request.getUsername());
                return ResponseEntity.badRequest().body("Username already exists");
            } catch (RuntimeException e) {
                // Username doesn't exist, which is good
                System.out.println("Username is available");
            }
            
            // Create User entity
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(request.getRole());
            
            // Set division if provided
            if (request.getDivisionId() != null) {
                try {
                    Division division = divisionService.getDivisionById(request.getDivisionId());
                    user.setDivision(division);
                    System.out.println("Division set: " + division.getName());
                } catch (Exception e) {
                    System.err.println("Division not found: " + request.getDivisionId());
                    return ResponseEntity.badRequest().body("Division not found with ID: " + request.getDivisionId());
                }
            }
            
            User createdUser = userService.createUser(user);
            System.out.println("User created successfully with ID: " + createdUser.getId());
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
            
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating user: " + e.getMessage());
        }
    }

    /**
     * Update user
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Integer id, @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    // @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}