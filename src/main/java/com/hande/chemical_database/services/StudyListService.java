package com.hande.chemical_database.services;

import com.hande.chemical_database.entities.StudyList;
import com.hande.chemical_database.models.StudyListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface StudyListService {

    /**
     * Create a new study
     */
    StudyListDTO createStudyList(StudyListDTO studyList);

    /**
     * Update an existing study
     */
    Optional<StudyListDTO> updateStudyList(StudyListDTO studyList);

    /**
     * Get all studies
     */
    List<StudyListDTO> getAllStudyLists();

    /**
     * Get study by ID
     */
    Optional<StudyListDTO> getStudyListById(Long id);

    /**
     * Get study by study code
     */
    Optional<StudyListDTO> getStudyListByCode(String studyCode);

    /**
     * Delete study by ID
     */
    boolean deleteStudyList(Long id);

    /**
     * Delete study by study code
     */
    boolean deleteStudyListByCode(String studyCode);

    /**
     * Search studies by study code with pagination
     */
    Page<StudyList> listStudyListsByCode(String filterCriteria, Pageable pageRequest);

    /**
     * Check if study exists by study code
     */
    boolean existsByStudyCode(String studyCode);

    /**
     * Get count of all studies
     */
    long getStudyCount();

    /**
     * Get studies by status
     */
    List<StudyListDTO> getStudyListsByStatus(String status);

    /**
     * Get studies by risk level
     */
    List<StudyListDTO> getStudyListsByRiskLevel(String riskLevel);

    /**
     * Get studies by responsible person
     */
    List<StudyListDTO> getStudyListsByResponsiblePerson(String responsiblePerson);
}