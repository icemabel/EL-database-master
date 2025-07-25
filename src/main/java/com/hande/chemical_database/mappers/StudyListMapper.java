package com.hande.chemical_database.mappers;

import com.hande.chemical_database.entities.StudyList;
import com.hande.chemical_database.models.StudyListDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StudyListMapper {
    StudyList studyListDTOToStudyList(StudyListDTO dto);
    StudyListDTO studyListToStudyListDTO(StudyList studyList);
}