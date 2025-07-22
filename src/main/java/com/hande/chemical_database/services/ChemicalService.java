package com.hande.chemical_database.services;

import com.hande.chemical_database.entities.Chemicals;
import com.hande.chemical_database.models.ChemicalDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/*
 * 17/02/2025
 * handebarkan
 */
public interface ChemicalService {

    ChemicalDTO createChemicals(ChemicalDTO chemical);

    Optional<ChemicalDTO> updateChemicals(ChemicalDTO chemical);

    List<ChemicalDTO> getAllChemicals();

    Optional<ChemicalDTO> getChemicalById(Long id);

    boolean deleteChemical(Long id);
    boolean deleteChemicalByName(String name);

    Page<Chemicals> listChemicalsByName(String filterCriteria, Pageable pageRequest);
}

