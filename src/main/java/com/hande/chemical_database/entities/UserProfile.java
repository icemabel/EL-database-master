package com.hande.chemical_database.entities;

import com.hande.chemical_database.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profile")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "first_name", length = 100)
    private String first_name;

    @Column(name = "last_name", length = 100)
    private String last_name;

    @Column(name = "email", unique = true, length = 255)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phone_number;

    @Column(name = "position", length = 100)
    private String position;

    @Column(name = "duration")
    @Builder.Default
    private int duration = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method to get role as string for compatibility
    public String getRoleString() {
        return role != null ? role.getSimpleName() : UserRole.USER.getSimpleName();
    }

    // Helper method to set role from string
    public void setRoleFromString(String roleString) {
        this.role = UserRole.fromString(roleString);
    }

    // Helper method to get Spring Security role name
    public String getSpringSecurityRole() {
        return role != null ? role.getRoleName() : UserRole.USER.getRoleName();
    }
}