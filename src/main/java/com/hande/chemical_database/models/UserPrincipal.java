package com.hande.chemical_database.models;

import com.hande.chemical_database.entities.UserProfile;
import com.hande.chemical_database.enums.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Implementation of Spring Security's UserDetails interface
 * This class adapts our UserProfile entity to work with Spring Security
 */
@Slf4j
public class UserPrincipal implements UserDetails {

    private static final long serialVersionUID = 1L;
    private final UserProfile user;

    public UserPrincipal(UserProfile user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Use the role from user profile, with fallback logic
        UserRole userRole;

        if (user.getRole() != null) {
            userRole = user.getRole();
            log.debug("User {} has role: {}", user.getUsername(), userRole);
        } else if ("admin".equals(user.getUsername())) {
            // Fallback: grant admin role to 'admin' username
            userRole = UserRole.ADMIN;
            log.warn("User {} has no role in database, assigning ADMIN based on username", user.getUsername());
        } else {
            // Default to USER role
            userRole = UserRole.USER;
            log.warn("User {} has no role in database, assigning default USER role", user.getUsername());
        }

        String roleName = userRole.getRoleName();
        log.debug("Granting authority {} to user {}", roleName, user.getUsername());

        return Collections.singleton(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Account never expires
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Account is never locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Credentials never expire
    }

    @Override
    public boolean isEnabled() {
        return true; // Account is always enabled
    }

    // Getter for the underlying user profile
    public UserProfile getUserProfile() {
        return user;
    }

    // Convenience method to get user ID
    public Long getUserId() {
        return user.getId();
    }

    // Convenience method to get full name
    public String getFullName() {
        String firstName = user.getFirst_name();
        String lastName = user.getLast_name();

        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return user.getUsername();
        }
    }

    // Check if user has admin role
    public boolean isAdmin() {
        return getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    // Get user role as enum
    public UserRole getUserRole() {
        return user.getRole() != null ? user.getRole() : UserRole.USER;
    }

    // Get simple role name (without ROLE_ prefix)
    public String getSimpleRoleName() {
        return getUserRole().getSimpleName();
    }
}