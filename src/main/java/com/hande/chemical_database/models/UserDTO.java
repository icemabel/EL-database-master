package com.hande.chemical_database.models;

import com.hande.chemical_database.enums.UserRole;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private String username;
    private String password;
    private String first_name;
    private String last_name;
    private String email;
    private String phone_number;
    private String position;
    private int duration;
    @Builder.Default
    private UserRole role = UserRole.USER;
}
