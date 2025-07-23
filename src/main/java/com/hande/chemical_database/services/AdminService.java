package com.hande.chemical_database.services;

import com.hande.chemical_database.entities.UserProfile;
import com.hande.chemical_database.models.UserDTO;
import com.hande.chemical_database.repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public List<UserProfile> getAllUsers() {
        return userRepo.findAll();
    }

    public Optional<UserProfile> getUserById(Long id) {
        return userRepo.findById(id);
    }

    public Optional<UserProfile> getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public UserProfile createUser(UserDTO userDTO) {
        // Check if username already exists
        if (userRepo.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + userDTO.getUsername());
        }

        UserProfile user = UserProfile.builder()
                .username(userDTO.getUsername().trim())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .first_name(userDTO.getFirst_name())
                .last_name(userDTO.getLast_name())
                .email(userDTO.getEmail())
                .phone_number(userDTO.getPhone_number())
                .position(userDTO.getPosition())
                .duration(userDTO.getDuration())
                .build();

        UserProfile savedUser = userRepo.save(user);
        log.info("Admin created new user: {}", savedUser.getUsername());
        return savedUser;
    }

    public UserProfile updateUser(Long userId, UserDTO userDTO) {
        UserProfile existingUser = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Check if username is being changed and if new username already exists
        if (!existingUser.getUsername().equals(userDTO.getUsername())) {
            if (userRepo.findByUsername(userDTO.getUsername()).isPresent()) {
                throw new RuntimeException("Username already exists: " + userDTO.getUsername());
            }
        }

        // Update user fields
        existingUser.setUsername(userDTO.getUsername().trim());
        existingUser.setFirst_name(userDTO.getFirst_name());
        existingUser.setLast_name(userDTO.getLast_name());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setPhone_number(userDTO.getPhone_number());
        existingUser.setPosition(userDTO.getPosition());
        existingUser.setDuration(userDTO.getDuration());

        // Update password only if provided
        if (userDTO.getPassword() != null && !userDTO.getPassword().trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        UserProfile updatedUser = userRepo.save(existingUser);
        log.info("Admin updated user: {}", updatedUser.getUsername());
        return updatedUser;
    }

    public boolean deleteUser(Long userId) {
        Optional<UserProfile> user = userRepo.findById(userId);
        if (user.isPresent()) {
            // Don't allow deletion of admin user
            if ("admin".equals(user.get().getUsername())) {
                throw new RuntimeException("Cannot delete admin user");
            }

            userRepo.deleteById(userId);
            log.info("Admin deleted user: {}", user.get().getUsername());
            return true;
        }
        return false;
    }

    public boolean resetUserPassword(Long userId, String newPassword) {
        Optional<UserProfile> userOpt = userRepo.findById(userId);
        if (userOpt.isPresent()) {
            UserProfile user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepo.save(user);

            log.info("Admin reset password for user: {}", user.getUsername());
            return true;
        }
        return false;
    }

    public long getUserCount() {
        return userRepo.count();
    }

    public List<UserProfile> getUsersByPosition(String position) {
        // This would require adding a method to UserRepo
        return userRepo.findAll().stream()
                .filter(user -> position.equals(user.getPosition()))
                .toList();
    }

    public void createDefaultAdminIfNotExists() {
        // Check if admin user exists
        Optional<UserProfile> adminUser = userRepo.findByUsername("admin");

        if (adminUser.isEmpty()) {
            UserProfile admin = UserProfile.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("password"))
                    .first_name("Admin")
                    .last_name("User")
                    .email("admin@example.com")
                    .phone_number("123-456-7890")
                    .position("Administrator")
                    .duration(0)
                    .build();

            userRepo.save(admin);
            log.info("Default admin user created");
        }
    }
}