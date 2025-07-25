package com.hande.chemical_database.services;

import com.hande.chemical_database.entities.StudyList;
import com.hande.chemical_database.models.StudyListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface StudyListService {

    StudyListDTO createStudyList(StudyListDTO studyList);

    Optional<StudyListDTO> updateStudyList(StudyListDTO studyList);

    List<StudyListDTO> getAllStudyLists();

    Optional<StudyListDTO> getStudyListById(Long id);

    boolean deleteStudyList(Long id);

    boolean deleteStudyListByCode(String studyCode);

    Page<StudyList> listStudyListsByCode(String filterCriteria, Pageable pageRequest);
}