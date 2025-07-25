package com.hande.chemical_database.services;

import com.hande.chemical_database.entities.StudyList;
import com.hande.chemical_database.repositories.StudyListRepo;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudyListUploadCsvImpl implements StudyListUploadCsv {

    private final StudyListRepo studyListRepo;

    @Override
    public Integer uploadStudyLists(MultipartFile file) {
        validateFile(file);

        Set<StudyList> studyListsSet;
        try {
            studyListsSet = parseCsv(file);
        } catch (IOException e) {
            log.error("Error reading CSV file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Error reading CSV file: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error parsing CSV file: {}", file.getOriginalFilename(), e);
            throw new IllegalArgumentException("Error parsing CSV file: " + e.getMessage(), e);
        }

        if (studyListsSet.isEmpty()) {
            throw new IllegalArgumentException("No valid study records found in CSV file");
        }

        // Check for existing studies and filter them out
        List<String> existingStudyCodes = studyListRepo.findAllStudyCodes();
        Set<StudyList> newStudies = studyListsSet.stream()
                .filter(study -> !existingStudyCodes.contains(study.getStudyCode()))
                .collect(Collectors.toSet());

        if (newStudies.isEmpty()) {
            log.info("All studies in CSV already exist in database");
            return 0;
        }

        // Save new studies
        List<StudyList> savedStudies = studyListRepo.saveAll(newStudies);

        int skippedCount = studyListsSet.size() - newStudies.size();
        log.info("CSV upload completed: {} new studies saved, {} existing studies skipped",
                savedStudies.size(), skippedCount);

        return savedStudies.size();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("File must be a CSV file (.csv extension required)");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
    }

    private Set<StudyList> parseCsv(MultipartFile file) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            HeaderColumnNameMappingStrategy<StudyListCsvRecord> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(StudyListCsvRecord.class);

            CsvToBean<StudyListCsvRecord> csvToBean = new CsvToBeanBuilder<StudyListCsvRecord>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreEmptyLine(true)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withThrowExceptions(false)
                    .build();

            List<StudyListCsvRecord> csvRecords = csvToBean.parse();

            // Check for parsing exceptions
            List<CsvException> capturedExceptions = csvToBean.getCapturedExceptions();
            if (!capturedExceptions.isEmpty()) {
                log.warn("Found {} parsing errors in CSV file", capturedExceptions.size());
                for (CsvException exception : capturedExceptions) {
                    log.warn("CSV parsing error at line {}: {}", exception.getLineNumber(), exception.getMessage());
                }
            }

            // Filter duplicates and validate records
            Set<String> seenStudyCodes = new HashSet<>();
            return csvRecords.stream()
                    .filter(this::validateCsvRecord)
                    .map(this::convertCsvRecordToStudyList)
                    .filter(study -> study != null && seenStudyCodes.add(study.getStudyCode()))
                    .collect(Collectors.toSet());
        }
    }

    private boolean validateCsvRecord(StudyListCsvRecord record) {
        if (record == null) {
            log.warn("Null CSV record found, skipping");
            return false;
        }

        if (record.getStudyCode() == null || record.getStudyCode().trim().isEmpty()) {
            log.warn("CSV record missing required 'studyCode' field, skipping");
            return false;
        }

        return true;
    }

    private StudyList convertCsvRecordToStudyList(StudyListCsvRecord csvRecord) {
        try {
            return StudyList.builder()
                    .studyCode(csvRecord.getStudyCode().trim())
                    .documentCodes(trimOrNull(csvRecord.getDocumentCodes()))
                    .materialType(trimOrNull(csvRecord.getMaterialType()))
                    .studyLevel(trimOrNull(csvRecord.getStudyLevel()))
                    .riskLevel(trimOrNull(csvRecord.getRiskLevel()))
                    .info(trimOrNull(csvRecord.getInfo()))
                    .numberOfSamples(trimOrNull(csvRecord.getNumberOfSamples()))
                    .objectOfStudy(trimOrNull(csvRecord.getObjectOfStudy()))
                    .responsiblePerson(trimOrNull(csvRecord.getResponsiblePerson()))
                    .status(trimOrNull(csvRecord.getStatus()))
                    .build();

        } catch (Exception e) {
            log.error("Error converting CSV record to StudyList: {}", csvRecord, e);
            return null;
        }
    }

    private String trimOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    // Inner class for CSV mapping
    public static class StudyListCsvRecord {
        private String studyCode;
        private String documentCodes;
        private String materialType;
        private String studyLevel;
        private String riskLevel;
        private String info;
        private String numberOfSamples;
        private String objectOfStudy;
        private String responsiblePerson;
        private String status;

        // Getters and setters
        public String getStudyCode() { return studyCode; }
        public void setStudyCode(String studyCode) { this.studyCode = studyCode; }

        public String getDocumentCodes() { return documentCodes; }
        public void setDocumentCodes(String documentCodes) { this.documentCodes = documentCodes; }

        public String getMaterialType() { return materialType; }
        public void setMaterialType(String materialType) { this.materialType = materialType; }

        public String getStudyLevel() { return studyLevel; }
        public void setStudyLevel(String studyLevel) { this.studyLevel = studyLevel; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

        public String getInfo() { return info; }
        public void setInfo(String info) { this.info = info; }

        public String getNumberOfSamples() { return numberOfSamples; }
        public void setNumberOfSamples(String numberOfSamples) { this.numberOfSamples = numberOfSamples; }

        public String getObjectOfStudy() { return objectOfStudy; }
        public void setObjectOfStudy(String objectOfStudy) { this.objectOfStudy = objectOfStudy; }

        public String getResponsiblePerson() { return responsiblePerson; }
        public void setResponsiblePerson(String responsiblePerson) { this.responsiblePerson = responsiblePerson; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        @Override
        public String toString() {
            return "StudyListCsvRecord{" +
                    "studyCode='" + studyCode + '\'' +
                    ", materialType='" + materialType + '\'' +
                    ", status='" + status + '\'' +
                    '}';
        }
    }
}