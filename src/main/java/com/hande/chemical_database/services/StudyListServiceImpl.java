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

    // Make StudyListQRCodeService optional to avoid circular dependency
    private StudyListQRCodeService qrCodeService;

    // Setter injection for QR code service to avoid circular dependency
    public void setQrCodeService(StudyListQRCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @Override
    public StudyListDTO createStudyList(StudyListDTO studyListDTO) {
        try {
            // Check if study code already exists
            if (studyListDTO.getStudyCode() != null &&
                    studyListRepo.existsByStudyCodeIgnoreCase(studyListDTO.getStudyCode())) {
                throw new IllegalArgumentException("Study with code '" + studyListDTO.getStudyCode() + "' already exists");
            }

            StudyList studyList = studyListMapper.studyListDTOToStudyList(studyListDTO);
            StudyList savedStudyList = studyListRepo.save(studyList);

            log.info("Successfully created study with ID: {} and studyCode: {}",
                    savedStudyList.getId(), savedStudyList.getStudyCode());

            // Auto-generate QR code for new study (if service is available)
            if (qrCodeService != null) {
                try {
                    qrCodeService.generateQRCodeForStudyList(savedStudyList.getId());
                    // Reload the study to get the QR code info
                    savedStudyList = studyListRepo.findById(savedStudyList.getId()).orElse(savedStudyList);
                    log.info("QR code auto-generated for new study: {}", savedStudyList.getStudyCode());
                } catch (Exception e) {
                    log.warn("Failed to generate QR code for new study {}: {}",
                            savedStudyList.getStudyCode(), e.getMessage());
                }
            }

            return studyListMapper.studyListToStudyListDTO(savedStudyList);

        } catch (Exception e) {
            log.error("Error creating study: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<StudyListDTO> updateStudyList(StudyListDTO studyListDTO) {
        try {
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

        } catch (Exception e) {
            log.error("Error updating study: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<StudyListDTO> getAllStudyLists() {
        try {
            List<StudyList> studies = studyListRepo.findAll();
            log.info("Retrieved {} studies from database", studies.size());
            return studies.stream()
                    .map(studyListMapper::studyListToStudyListDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting all studies: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<StudyListDTO> getStudyListById(Long id) {
        try {
            Optional<StudyList> studyList = studyListRepo.findById(id);
            if (studyList.isPresent()) {
                log.info("Found study with ID: {}", id);
                return Optional.of(studyListMapper.studyListToStudyListDTO(studyList.get()));
            } else {
                log.info("Study not found with ID: {}", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error getting study by ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Optional<StudyListDTO> getStudyListByCode(String studyCode) {
        try {
            Optional<StudyList> studyList = studyListRepo.findByStudyCodeIgnoreCase(studyCode);
            if (studyList.isPresent()) {
                log.info("Found study with code: {}", studyCode);
                return Optional.of(studyListMapper.studyListToStudyListDTO(studyList.get()));
            } else {
                log.info("Study not found with code: {}", studyCode);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error getting study by code {}: {}", studyCode, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean deleteStudyList(Long id) {
        try {
            Optional<StudyList> studyListDb = studyListRepo.findById(id);
            if (studyListDb.isPresent()) {
                studyListRepo.deleteById(id);
                log.info("Successfully deleted study with ID: {}", id);
                return true;
            } else {
                log.warn("Study not found for deletion with ID: {}", id);
                throw new ResourceNotFoundException("Record not found with id : " + id);
            }
        } catch (Exception e) {
            log.error("Error deleting study with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean deleteStudyListByCode(String studyCode) {
        try {
            if (studyListRepo.existsByStudyCodeIgnoreCase(studyCode)) {
                studyListRepo.deleteByStudyCode(studyCode);
                log.info("Successfully deleted study with code: {}", studyCode);
                return true;
            } else {
                log.warn("Study not found for deletion with code: {}", studyCode);
                throw new ResourceNotFoundException("Record not found with study code : " + studyCode);
            }
        } catch (Exception e) {
            log.error("Error deleting study with code {}: {}", studyCode, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Page<StudyList> listStudyListsByCode(String filterCriteria, Pageable pageRequest) {
        try {
            Page<StudyList> studies = studyListRepo.findByStudyCodeContainingIgnoreCase(filterCriteria, pageRequest);
            log.info("Found {} studies matching filter: '{}'", studies.getTotalElements(), filterCriteria);
            return studies;
        } catch (Exception e) {
            log.error("Error searching studies with filter '{}': {}", filterCriteria, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean existsByStudyCode(String studyCode) {
        try {
            boolean exists = studyListRepo.existsByStudyCodeIgnoreCase(studyCode);
            log.debug("Study code '{}' exists: {}", studyCode, exists);
            return exists;
        } catch (Exception e) {
            log.error("Error checking if study code exists '{}': {}", studyCode, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public long getStudyCount() {
        try {
            long count = studyListRepo.count();
            log.info("Total study count: {}", count);
            return count;
        } catch (Exception e) {
            log.error("Error getting study count: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<StudyListDTO> getStudyListsByStatus(String status) {
        try {
            // Using the existing pagination method with a filter but getting all results
            Page<StudyList> studies = studyListRepo.findByStatusContainingIgnoreCase(status, Pageable.unpaged());
            log.info("Found {} studies with status: {}", studies.getTotalElements(), status);
            return studies.getContent().stream()
                    .map(studyListMapper::studyListToStudyListDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting studies by status '{}': {}", status, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<StudyListDTO> getStudyListsByRiskLevel(String riskLevel) {
        try {
            Page<StudyList> studies = studyListRepo.findByRiskLevel(riskLevel, Pageable.unpaged());
            log.info("Found {} studies with risk level: {}", studies.getTotalElements(), riskLevel);
            return studies.getContent().stream()
                    .map(studyListMapper::studyListToStudyListDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting studies by risk level '{}': {}", riskLevel, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<StudyListDTO> getStudyListsByResponsiblePerson(String responsiblePerson) {
        try {
            Page<StudyList> studies = studyListRepo.findByResponsiblePersonContainingIgnoreCase(responsiblePerson, Pageable.unpaged());
            log.info("Found {} studies with responsible person: {}", studies.getTotalElements(), responsiblePerson);
            return studies.getContent().stream()
                    .map(studyListMapper::studyListToStudyListDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting studies by responsible person '{}': {}", responsiblePerson, e.getMessage(), e);
            throw e;
        }
    }
}