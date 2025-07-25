package com.hande.chemical_database.controllers;

// UserController temporarily disabled for testing without authentication
// Uncomment and fix when adding authentication back

/*

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

    // Web pages for authentication
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // API endpoints for authentication
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

            // Check if user already exists
            if (userDetailsService.userExists(userDTO.getUsername().trim())) {
                response.put("error", "Username already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // Create user profile with explicit USER role
            UserProfile user = UserProfile.builder()
                    .username(userDTO.getUsername().trim())
                    .password(userDTO.getPassword())
                    .first_name(userDTO.getFirst_name())
                    .last_name(userDTO.getLast_name())
                    .email(userDTO.getEmail().trim())
                    .phone_number(userDTO.getPhone_number())
                    .position(userDTO.getPosition() != null ? userDTO.getPosition() : "User")
                    .duration(userDTO.getDuration())
                    .role(UserRole.USER) // Explicitly set USER role
                    .build();

            UserProfile savedUser = userDetailsService.saveUser(user);

            log.info("New user registered: {} with role: {}", savedUser.getUsername(), savedUser.getRole());

            response.put("message", "User registered successfully");
            response.put("username", savedUser.getUsername());
            response.put("role", savedUser.getRole().toString());

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
    public ResponseEntity<Map<String, String>> login(@RequestBody UserDTO userDTO) {
        Map<String, String> response = new HashMap<>();

        try {
            // Validate input
            if (userDTO.getUsername() == null || userDTO.getPassword() == null) {
                response.put("error", "Username and password are required");
                return ResponseEntity.badRequest().body(response);
            }

            String username = userDTO.getUsername().trim();

            // Debug: Check user in database before authentication
            var userProfile = userDetailsService.getUserByUsername(username);
            if (userProfile.isPresent()) {
                log.debug("User found in database: {} with role: {}", username, userProfile.get().getRole());
            } else {
                log.warn("User not found in database: {}", username);
                response.put("error", "Invalid username or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Authenticate user
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, userDTO.getPassword())
            );

            if (authentication.isAuthenticated()) {
                // Generate JWT token
                String token = jwtService.generateToken(username);

                // Get user details for response
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                // Debug: Log authorities
                log.debug("User authenticated: {} with authorities: {}",
                        userDetails.getUsername(), userDetails.getAuthorities());

                response.put("token", token);
                response.put("username", userDetails.getUsername());
                response.put("message", "Login successful");

                // Add role information to response
                String roleInfo = userDetails.getAuthorities().toString();
                response.put("authorities", roleInfo);

                log.info("User logged in: {} with authorities: {}", userDetails.getUsername(), roleInfo);

                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Authentication failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {} - {}", userDTO.getUsername(), e.getMessage());
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
            response.put("authenticated", true);

            // Add additional user profile information
            var userProfile = userDetailsService.getUserByUsername(username);
            if (userProfile.isPresent()) {
                UserProfile user = userProfile.get();
                response.put("email", user.getEmail());
                response.put("firstName", user.getFirst_name());
                response.put("lastName", user.getLast_name());
                response.put("position", user.getPosition());
                response.put("role", user.getRole().toString());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting user profile", e);
            response.put("error", "Failed to get profile");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

 */