package com.hande.chemical_database.repositories;

import com.hande.chemical_database.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 * 16/02/2025
 * handebarkan
 */
@Repository
public interface UserRepo extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUsername(String username);
}
