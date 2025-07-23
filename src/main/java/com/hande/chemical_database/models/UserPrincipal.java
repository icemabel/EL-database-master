package com.hande.chemical_database.models;

import com.hande.chemical_database.entities.UserProfile;
import com.hande.chemical_database.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private static final long serialVersionUID = 1L;
    private final UserProfile user;

    public UserPrincipal(UserProfile user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Use the role from user profile
        UserRole userRole = user.getRole() != null ? user.getRole() : UserRole.USER;
        return Collections.singleton(new SimpleGrantedAuthority(userRole.getRoleName()));
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
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Getter for the underlying user profile
    public UserProfile getUserProfile() {
        return user;
    }

    // Convenience methods
    public Long getUserId() {
        return user.getId();
    }

    public UserRole getUserRole() {
        return user.getRole();
    }

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

    // Simplified role checking
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(user.getRole());
    }

    public boolean canDelete() {
        return UserRole.ADMIN.equals(user.getRole());
    }
}