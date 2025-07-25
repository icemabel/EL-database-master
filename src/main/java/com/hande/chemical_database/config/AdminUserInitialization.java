package com.hande.chemical_database.config;

import com.hande.chemical_database.entities.UserProfile;
import com.hande.chemical_database.enums.UserRole;
import com.hande.chemical_database.repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

//@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Run this first
public class AdminUserInitialization implements CommandLineRunner {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createAdminUserIfNotExists();
        createTestUsersIfNotExists();
        verifyAllUsers();
    }

    private void createAdminUserIfNotExists() {
        try {
            if (!userRepo.findByUsername("admin").isPresent()) {
                UserProfile admin = UserProfile.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("password"))
                        .first_name("Admin")
                        .last_name("User")
                        .email("admin@example.com")
                        .phone_number("123-456-7890")
                        .position("Administrator")
                        .duration(0)
                        .role(UserRole.ADMIN)
                        .build();

                UserProfile savedAdmin = userRepo.save(admin);
                log.info("✅ Admin user created: {} with role: {}", savedAdmin.getUsername(), savedAdmin.getRole());
            } else {
                // Update existing admin user to ensure correct role and password
                UserProfile existingAdmin = userRepo.findByUsername("admin").get();
                boolean updated = false;

                if (existingAdmin.getRole() != UserRole.ADMIN) {
                    existingAdmin.setRole(UserRole.ADMIN);
                    updated = true;
                    log.info("Updated admin role to ADMIN");
                }

                // Reset admin password to "password" (useful for testing)
                existingAdmin.setPassword(passwordEncoder.encode("password"));
                updated = true;

                if (updated) {
                    userRepo.save(existingAdmin);
                    log.info("✅ Admin user updated with correct role and password");
                }

                log.info("✅ Admin user exists: {} with role: {}", existingAdmin.getUsername(), existingAdmin.getRole());
            }
        } catch (Exception e) {
            log.error("❌ Error creating/updating admin user", e);
        }
    }

    private void createTestUsersIfNotExists() {
        String[][] testUsers = {
                {"labtech1", "John", "Doe", "john.doe@example.com", "Lab Technician"},
                {"scientist1", "Jane", "Smith", "jane.smith@example.com", "Senior Scientist"},
                {"safety1", "Bob", "Johnson", "bob.johnson@example.com", "Safety Officer"}
        };

        for (String[] userData : testUsers) {
            try {
                if (!userRepo.findByUsername(userData[0]).isPresent()) {
                    UserProfile user = UserProfile.builder()
                            .username(userData[0])
                            .password(passwordEncoder.encode("password"))
                            .first_name(userData[1])
                            .last_name(userData[2])
                            .email(userData[3])
                            .phone_number("123-456-789" + userData[0].charAt(userData[0].length() - 1))
                            .position(userData[4])
                            .duration(2)
                            .role(UserRole.USER)
                            .build();

                    UserProfile savedUser = userRepo.save(user);
                    log.info("✅ Test user created: {} with role: {}", savedUser.getUsername(), savedUser.getRole());
                }
            } catch (Exception e) {
                log.error("❌ Error creating test user: {}", userData[0], e);
            }
        }
    }

    private void verifyAllUsers() {
        log.info("🔍 Verifying all users in database:");
        userRepo.findAll().forEach(user -> {
            log.info("User: {} | Role: {} | Email: {}",
                    user.getUsername(),
                    user.getRole(),
                    user.getEmail());
        });
    }
}