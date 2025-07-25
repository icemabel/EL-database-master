package com.hande.chemical_database.controllers;
// DebugController temporarily disabled for testing without authentication
// Uncomment and fix when adding authentication back

/*

import com.hande.chemical_database.entities.UserProfile;
import com.hande.chemical_database.models.UserPrincipal;
import com.hande.chemical_database.repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Slf4j
public class DebugController {

    private final UserRepo userRepo;

    @GetMapping("/auth")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugAuth(Authentication authentication) {
        Map<String, Object> debugInfo = new HashMap<>();

        try {
            // Get authentication from SecurityContext
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            debugInfo.put("authFromParameter", authentication != null);
            debugInfo.put("authFromSecurityContext", auth != null);

            if (auth != null) {
                debugInfo.put("username", auth.getName());
                debugInfo.put("authenticated", auth.isAuthenticated());
                debugInfo.put("authorities", auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
                debugInfo.put("principalType", auth.getPrincipal().getClass().getSimpleName());

                if (auth.getPrincipal() instanceof UserPrincipal) {
                    UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
                    debugInfo.put("userRole", userPrincipal.getUserRole());
                    debugInfo.put("isAdmin", userPrincipal.isAdmin());
                    debugInfo.put("fullName", userPrincipal.getFullName());
                }
            } else {
                debugInfo.put("error", "No authentication found");
            }

            return ResponseEntity.ok(debugInfo);

        } catch (Exception e) {
            log.error("Error in debug auth endpoint", e);
            debugInfo.put("error", "Exception: " + e.getMessage());
            return ResponseEntity.ok(debugInfo);
        }
    }

    @GetMapping("/users")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> listAllUsers() {
        try {
            // This is for debugging - normally you'd restrict this to admins
            List<UserProfile> users = userRepo.findAll();

            List<Map<String, Object>> userList = users.stream()
                    .map(user -> {
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("username", user.getUsername());
                        userInfo.put("role", user.getRole());
                        userInfo.put("email", user.getEmail());
                        userInfo.put("firstName", user.getFirst_name());
                        userInfo.put("lastName", user.getLast_name());
                        userInfo.put("position", user.getPosition());
                        // Don't include password for security
                        return userInfo;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(userList);

        } catch (Exception e) {
            log.error("Error listing users", e);
            return ResponseEntity.status(500).build();
        }
    }

}

 */