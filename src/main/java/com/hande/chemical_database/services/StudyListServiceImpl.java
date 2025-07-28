package com.hande.chemical_database.services;

import com.hande.chemical_database.exceptions.ResourceNotFoundException;
import com.hande.chemical_database.entities.StudyList;
import com.hande.chemical_database.mappers.StudyListMapper;
import com.hande.chemical_database.models.StudyListDTO;
import com.hande.chemical_database.repositories.StudyListRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudyListServiceImpl implements StudyListService {

    private final StudyListRepo studyListRepo;
    private final StudyListMapper studyListMapper;
    private final StudyListQRCodeService qrCodeService;

    @Override
    public StudyListDTO createStudyList(StudyListDTO studyList) {
        StudyList savedStudyList = studyListRepo.save(studyListMapper.studyListDTOToStudyList(studyList));

        // Auto-generate QR code for new study
        try {
            qrCodeService.generateQRCodeForStudyList(savedStudyList.getId());
            // Reload the study to get the QR code info
            savedStudyList = studyListRepo.findById(savedStudyList.getId()).orElse(savedStudyList);
        } catch (Exception e) {
            log.warn("Failed to generate QR code for new study {}: {}", savedStudyList.getStudyCode(), e.getMessage());
        }

        return studyListMapper.studyListToStudyListDTO(savedStudyList);
    }

    // Replace the updateStudyList method in your StudyListServiceImpl.java with this:

    @Override
    public Optional<StudyListDTO> updateStudyList(StudyListDTO studyListDTO) {
        // Try to find by ID first, then by studyCode as fallback
        Optional<StudyList> studyListDb = Optional.empty();

        if (studyListDTO.getId() != null) {
            // If ID is provided, find by ID
            studyListDb = studyListRepo.findById(studyListDTO.getId());
            log.info("Looking for study by ID: {}", studyListDTO.getId());
        }

        if (studyListDb.isEmpty() && studyListDTO.getStudyCode() != null) {
            // If not found by ID or ID not provided, try by studyCode
            studyListDb = studyListRepo.findByStudyCodeIgnoreCase(studyListDTO.getStudyCode());
            log.info("Looking for study by studyCode: {}", studyListDTO.getStudyCode());
        }

        if (studyListDb.isPresent()) {
            StudyList studyListUpdate = studyListDb.get();

            // Update all fields
            studyListUpdate.setStudyCode(studyListDTO.getStudyCode());
            studyListUpdate.setDocumentCodes(studyListDTO.getDocumentCodes());
            studyListUpdate.setMaterialType(studyListDTO.getMaterialType());
            studyListUpdate.setStudyLevel(studyListDTO.getStudyLevel());
            studyListUpdate.setRiskLevel(studyListDTO.getRiskLevel());
            studyListUpdate.setInfo(studyListDTO.getInfo());
            studyListUpdate.setNumberOfSamples(studyListDTO.getNumberOfSamples());
            studyListUpdate.setObjectOfStudy(studyListDTO.getObjectOfStudy());
            studyListUpdate.setResponsiblePerson(studyListDTO.getResponsiblePerson());
            studyListUpdate.setStatus(studyListDTO.getStatus());

            StudyList updatedStudyList = studyListRepo.save(studyListUpdate);
            log.info("Successfully updated study with ID: {} and studyCode: {}",
                    updatedStudyList.getId(), updatedStudyList.getStudyCode());

            return Optional.of(studyListMapper.studyListToStudyListDTO(updatedStudyList));
        } else {
            String identifier = studyListDTO.getId() != null ?
                    "ID: " + studyListDTO.getId() :
                    "study code: " + studyListDTO.getStudyCode();
            throw new ResourceNotFoundException("Record not found with " + identifier);
        }
    }

    @Override
    public List<StudyListDTO> getAllStudyLists() {
        return studyListRepo.findAll().stream()
                .map(studyListMapper::studyListToStudyListDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StudyListDTO> getStudyListById(Long id) {
        return Optional.ofNullable(studyListMapper.studyListToStudyListDTO(studyListRepo.findById(id)
                .orElse(null)));
    }

    @Override
    public boolean deleteStudyList(Long id) {
        Optional<StudyList> studyListDb = studyListRepo.findById(id);
        if(studyListDb.isPresent()) {
            studyListRepo.deleteById(id);
            return true;
        } else {
            throw new ResourceNotFoundException("Record not found with id : " + id);
        }
    }

    @Override
    public boolean deleteStudyListByCode(String studyCode) {
        if(studyListRepo.existsByStudyCodeIgnoreCase(studyCode)) {
            studyListRepo.deleteByStudyCode(studyCode);
            return true;
        } else {
            throw new ResourceNotFoundException("Record not found with study code : " + studyCode);
        }
    }

    @Override
    public Page<StudyList> listStudyListsByCode(String filterCriteria, Pageable pageRequest) {
        return studyListRepo.findByStudyCodeContainingIgnoreCase(filterCriteria, pageRequest);
    }
}