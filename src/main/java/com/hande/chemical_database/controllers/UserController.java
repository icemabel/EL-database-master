package com.hande.chemical_database.controllers;

import com.hande.chemical_database.entities.UserProfile;
import com.hande.chemical_database.enums.UserRole;
import com.hande.chemical_database.models.UserDTO;
import com.hande.chemical_database.services.JwtService;
import com.hande.chemical_database.services.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final MyUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/api/register")
    @ResponseBody
    public ResponseEntity<Map<String, String>> register(@RequestBody UserDTO userDTO) {
        Map<String, String> response = new HashMap<>();

        try {
            // Validate input
            if (userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty()) {
                response.put("error", "Username is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (userDTO.getPassword() == null || userDTO.getPassword().length() < 6) {
                response.put("error", "Password must be at least 6 characters long");
                return ResponseEntity.badRequest().body(response);
            }

            if (userDTO.getEmail() == null || !userDTO.getEmail().contains("@")) {
                response.put("error", "Valid email is required");
                return ResponseEntity.badRequest().body(response);
            }

            // Set default role if not provided or invalid
            UserRole assignedRole = UserRole.USER; // Default role for new registrations
            if (userDTO.getRole() != null) {
                try {
                    assignedRole = userDTO.getRole();
                    // Only allow LAB_USER role for self-registration (security)
                    if (assignedRole != UserRole.USER) {
                        log.warn("User {} attempted to register with role {}, defaulting to LAB_USER",
                                userDTO.getUsername(), assignedRole);
                        assignedRole = UserRole.USER;
                    }
                } catch (Exception e) {
                    log.warn("Invalid role provided during registration, using default");
                    assignedRole = UserRole.USER;
                }
            }

            // Create user profile
            UserProfile user = UserProfile.builder()
                    .username(userDTO.getUsername().trim())
                    .password(userDTO.getPassword())
                    .first_name(userDTO.getFirst_name())
                    .last_name(userDTO.getLast_name())
                    .email(userDTO.getEmail().trim())
                    .phone_number(userDTO.getPhone_number())
                    .position(userDTO.getPosition() != null ? userDTO.getPosition() : "")
                    .duration(0)
                    .role(assignedRole)
                    .build();

            UserProfile savedUser = userDetailsService.saveUser(user);

            log.info("New user registered: {} with role: {}", savedUser.getUsername(), savedUser.getRole());

            response.put("message", "User registered successfully");
            response.put("username", savedUser.getUsername());
            response.put("role", savedUser.getRole().name());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error registering user: {}", userDTO.getUsername(), e);

            if (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("unique")) {
                response.put("error", "Username or email already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            response.put("error", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserDTO userDTO) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDTO.getUsername() == null || userDTO.getPassword() == null) {
                response.put("error", "Username and password are required");
                return ResponseEntity.badRequest().body(response);
            }

            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            userDTO.getUsername().trim(),
                            userDTO.getPassword()
                    )
            );

            if (authentication.isAuthenticated()) {
                String token = jwtService.generateToken(userDTO.getUsername().trim());
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                response.put("token", token);
                response.put("username", userDetails.getUsername());
                response.put("authorities", userDetails.getAuthorities());
                response.put("message", "Login successful");

                log.info("User logged in: {}", userDetails.getUsername());
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Authentication failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {}", userDTO.getUsername());
            response.put("error", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            log.error("Error during login for user: {}", userDTO.getUsername(), e);
            response.put("error", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/api/logout")
    @ResponseBody
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/profile")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProfile(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("error", "Not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String username = authentication.getName();
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            response.put("username", userDetails.getUsername());
            response.put("authorities", userDetails.getAuthorities());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting user profile", e);
            response.put("error", "Failed to get profile");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
