package com.hande.chemical_database.services;

import com.hande.chemical_database.enums.UserRole;
import com.hande.chemical_database.models.UserPrincipal;
import com.hande.chemical_database.entities.UserProfile;
import com.hande.chemical_database.repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

//@Service
@RequiredArgsConstructor
@Slf4j
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user: {}", username);

        Optional<UserProfile> userOpt = userRepo.findByUsername(username);

        if (userOpt.isEmpty()) {
            log.warn("User not found: {}", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }

        UserProfile user = userOpt.get();
        log.debug("User found: {}, Role: {}", user.getUsername(), user.getRole());

        UserPrincipal userPrincipal = new UserPrincipal(user);
        log.debug("UserPrincipal created with authorities: {}", userPrincipal.getAuthorities());

        return userPrincipal;
    }

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    public UserProfile saveUser(UserProfile user) {
        // Ensure the user has a role assigned
        if (user.getRole() == null) {
            log.warn("User {} has no role assigned, setting to USER", user.getUsername());
            user.setRole(UserRole.USER);
        }

        log.info("Saving new user: {} with role: {}", user.getUsername(), user.getRole());
        user.setPassword(encoder.encode(user.getPassword()));
        UserProfile savedUser = userRepo.save(user);
        log.info("User saved successfully: {} with ID: {} and role: {}",
                savedUser.getUsername(), savedUser.getId(), savedUser.getRole());

        // Verify the user was saved with correct role
        UserProfile verifyUser = userRepo.findById(savedUser.getId()).orElse(null);
        if (verifyUser != null) {
            log.debug("Verification - User {} saved with role: {}",
                    verifyUser.getUsername(), verifyUser.getRole());
        }

        return savedUser;
    }

    // Helper method to check if user exists
    public boolean userExists(String username) {
        return userRepo.findByUsername(username).isPresent();
    }

    // Helper method to get user by username
    public Optional<UserProfile> getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }
}