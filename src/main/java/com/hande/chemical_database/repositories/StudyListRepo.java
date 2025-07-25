package com.hande.chemical_database.repositories;

import com.hande.chemical_database.entities.StudyList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyListRepo extends JpaRepository<StudyList, Long> {

    Optional<StudyList> findByStudyCodeIgnoreCase(String studyCode);

    @Query("SELECT s.studyCode FROM StudyList s")
    List<String> findAllStudyCodes();

    Page<StudyList> findByStudyCodeContainingIgnoreCase(String studyCode, Pageable pageable);

    Page<StudyList> findByMaterialTypeContainingIgnoreCase(String materialType, Pageable pageable);

    Page<StudyList> findByResponsiblePersonContainingIgnoreCase(String responsiblePerson, Pageable pageable);

    Page<StudyList> findByStatusContainingIgnoreCase(String status, Pageable pageable);

    Page<StudyList> findByStudyLevel(String studyLevel, Pageable pageable);

    Page<StudyList> findByRiskLevel(String riskLevel, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM StudyList s WHERE s.studyCode = :studyCode")
    void deleteByStudyCode(@Param("studyCode") String studyCode);

    boolean existsByStudyCode(String studyCode);

    boolean existsByStudyCodeIgnoreCase(String studyCode);

    Optional<StudyList> findByQrCode(String qrCode);
}