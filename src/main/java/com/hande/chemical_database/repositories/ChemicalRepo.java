package com.hande.chemical_database.repositories;

import com.hande.chemical_database.entities.Chemicals;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChemicalRepo extends JpaRepository<Chemicals, Long> {

    Optional<Chemicals> findByNameIgnoreCase(String name);

    List<Chemicals> findByCasNo(String casNo);

    @Query("SELECT c.name FROM Chemicals c")
    List<String> findAllChemicalNames();

    Page<Chemicals> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Chemicals> findByStorageContainingIgnoreCase(String storage, Pageable pageable);

    Page<Chemicals> findByResponsibleContainingIgnoreCase(String responsible, Pageable pageable);

    Page<Chemicals> findByOrderDate(LocalDate orderDate, Pageable pageable);

    Page<Chemicals> findByToxicState(boolean toxicState, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM Chemicals c WHERE c.name = :name")
    void deleteByName(@Param("name") String name);

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);

    Optional<Chemicals> findByQrCode(String qrCode);

}