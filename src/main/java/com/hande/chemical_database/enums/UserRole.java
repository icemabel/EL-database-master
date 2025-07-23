package com.hande.chemical_database.enums;

public enum UserRole {
    ADMIN("Administrator - Full system access including delete operations"),
    USER("User - Full access except delete operations");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // Get Spring Security role name
    public String getRoleName() {
        return "ROLE_" + this.name();
    }
}
