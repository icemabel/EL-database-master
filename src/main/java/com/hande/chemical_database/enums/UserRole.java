package com.hande.chemical_database.enums;

/**
 * Enum for user roles in the system
 */
public enum UserRole {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private final String roleName;

    UserRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRole() {
        return roleName;
    }

    @Override
    public String toString() {
        return roleName;
    }

    public static UserRole fromString(String role) {
        if (role == null) {
            return USER;
        }

        // Handle both "USER"/"ADMIN" and "ROLE_USER"/"ROLE_ADMIN" formats
        String normalizedRole = role.toUpperCase();
        if (!normalizedRole.startsWith("ROLE_")) {
            normalizedRole = "ROLE_" + normalizedRole;
        }

        for (UserRole userRole : UserRole.values()) {
            if (userRole.roleName.equals(normalizedRole)) {
                return userRole;
            }
        }

        return USER; // Default to USER if not found
    }

    // Helper method to get simple role name without "ROLE_" prefix
    public String getSimpleName() {
        return roleName.replace("ROLE_", "");
    }
}