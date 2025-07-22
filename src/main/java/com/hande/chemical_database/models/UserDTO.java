package com.hande.chemical_database.models;

import lombok.*;

/*
 * 18/02/2025
 * handebarkan
 */
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

}
