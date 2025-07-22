package com.hande.chemical_database.mappers;

import com.hande.chemical_database.entities.UserProfile;
import com.hande.chemical_database.models.UserDTO;
import org.mapstruct.Mapper;

/*
 * 18/02/2025
 * handebarkan
 */
@Mapper
public interface UserMapper {

    UserProfile userDTOToUser(UserDTO dto);
    UserDTO userToUserDTO(UserProfile user);
}
