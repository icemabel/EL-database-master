package com.hande.chemical_database.mappers;

import com.hande.chemical_database.entities.Chemicals;
import com.hande.chemical_database.models.ChemicalDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChemicalMapper {
    // No explicit mappings needed now since field names match
    Chemicals chemicalDTOToChemical(ChemicalDTO dto);
    ChemicalDTO chemicalToChemicalDTO(Chemicals chemicals);
}