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

    @Override
    public Optional<StudyListDTO> updateStudyList(StudyListDTO studyList) {
        Optional<StudyList> studyListDb = studyListRepo.findByStudyCodeIgnoreCase(studyList.getStudyCode());
        if(studyListDb.isPresent()) {
            StudyList studyListUpdate = studyListDb.get();
            studyListUpdate.setStudyCode(studyList.getStudyCode());
            studyListUpdate.setDocumentCodes(studyList.getDocumentCodes());
            studyListUpdate.setMaterialType(studyList.getMaterialType());
            studyListUpdate.setStudyLevel(studyList.getStudyLevel());
            studyListUpdate.setRiskLevel(studyList.getRiskLevel());
            studyListUpdate.setInfo(studyList.getInfo());
            studyListUpdate.setNumberOfSamples(studyList.getNumberOfSamples());
            studyListUpdate.setObjectOfStudy(studyList.getObjectOfStudy());
            studyListUpdate.setResponsiblePerson(studyList.getResponsiblePerson());
            studyListUpdate.setStatus(studyList.getStatus());

            StudyList updatedStudyList = studyListRepo.save(studyListUpdate);
            return Optional.of(studyListMapper.studyListToStudyListDTO(updatedStudyList));
        } else {
            throw new ResourceNotFoundException("Record not found with study code : " + studyList.getStudyCode());
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