package com.hande.chemical_database.mappers;

import com.hande.chemical_database.entities.Chemicals;
import com.hande.chemical_database.models.ChemicalDTO;
import org.mapstruct.Mapper;

/*
 * 18/02/2025
 * handebarkan
 */
@Mapper(componentModel = "spring")
public interface ChemicalMapper {
    Chemicals chemicalDTOToChemical(ChemicalDTO dto);
    ChemicalDTO chemicalToChemicalDTO(Chemicals chemicals);
}
