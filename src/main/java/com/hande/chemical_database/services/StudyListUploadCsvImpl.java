package com.hande.chemical_database.services;

import com.hande.chemical_database.entities.StudyList;
import com.hande.chemical_database.repositories.StudyListRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
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
            studyListsSet = parseCsvWithDelimiterDetection(file);
        } catch (IOException e) {
            log.error("Error reading CSV file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Error reading CSV file: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error parsing CSV file: {}", file.getOriginalFilename(), e);
            throw new IllegalArgumentException("Error parsing CSV file: " + e.getMessage(), e);
        }

        if (studyListsSet.isEmpty()) {
            throw new IllegalArgumentException("No valid study records found in CSV file. Check that your file has the correct columns: Study Code, Document codes protocol & report, Material type, Study Level, Risk Level, Info, Number of Samples / Sample ID, Object of study, Responsible Person, Status");
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

    private Set<StudyList> parseCsvWithDelimiterDetection(MultipartFile file) throws IOException {
        Set<StudyList> studies = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            String line;
            boolean isFirstLine = true;
            String[] headers = null;
            char delimiter = ';'; // Default to semicolon for European Excel

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (isFirstLine) {
                    // Detect delimiter from first line
                    delimiter = detectDelimiter(line);
                    headers = parseCSVLine(line, delimiter);
                    isFirstLine = false;
                    log.info("CSV: Headers detected: {}", Arrays.toString(headers));
                    log.info("CSV: Using delimiter: '{}'", delimiter);
                    continue;
                }

                if (headers == null) {
                    continue;
                }

                // Parse CSV line with detected delimiter
                String[] values = parseCSVLine(line, delimiter);

                if (values.length == 0) {
                    continue;
                }

                // Create study object from CSV row
                StudyList study = createStudyFromCSVRow(headers, values);
                if (study != null) {
                    studies.add(study);
                }
            }
        }

        log.info("CSV: Processed {} studies from file", studies.size());
        return studies;
    }

    private char detectDelimiter(String line) {
        // Remove any carriage returns and newlines for analysis
        line = line.replaceAll("[\\r\\n]", "");

        int commaCount = 0;
        int semicolonCount = 0;
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                if (c == ',') {
                    commaCount++;
                } else if (c == ';') {
                    semicolonCount++;
                }
            }
        }

        log.info("CSV: Delimiter detection - Commas: {}, Semicolons: {}", commaCount, semicolonCount);

        // If we have semicolons, use semicolons (even if there are also commas)
        // This handles European CSV format preference
        if (semicolonCount > 0) {
            return ';';
        } else if (commaCount > 0) {
            return ',';
        } else {
            // Default to semicolon for European format
            return ';';
        }
    }

    private StudyList createStudyFromCSVRow(String[] headers, String[] values) {
        try {
            StudyList.StudyListBuilder builder = StudyList.builder();

            // Ensure we have enough values, pad with empty strings if necessary
            String[] paddedValues = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                if (i < values.length) {
                    paddedValues[i] = values[i];
                } else {
                    paddedValues[i] = ""; // Fill missing values with empty string
                }
            }

            for (int i = 0; i < headers.length; i++) {
                String header = cleanHeader(headers[i]);
                String value = cleanValue(paddedValues[i]);

                // Map Excel column names exactly as they appear
                switch (header) {
                    case "study code":
                        builder.studyCode(value);
                        break;
                    case "document codes protocol & report":
                        builder.documentCodes(value);
                        break;
                    case "material type":
                        builder.materialType(value);
                        break;
                    case "study level":
                        builder.studyLevel(value);
                        break;
                    case "risk level":
                        builder.riskLevel(value);
                        break;
                    case "info":
                        builder.info(value);
                        break;
                    case "number of samples / sample id":
                    case "number of samples":
                    case "sample id":
                        builder.numberOfSamples(value);
                        break;
                    case "object of study":
                        builder.objectOfStudy(value);
                        break;
                    case "responsible person":
                        builder.responsiblePerson(value);
                        break;
                    case "status":
                        builder.status(value);
                        break;
                    default:
                        log.debug("CSV: Unmapped column: '{}' with value: '{}'", header, value);
                }
            }

            StudyList study = builder.build();

            // Validate required fields
            if (study.getStudyCode() == null || study.getStudyCode().trim().isEmpty()) {
                log.warn("CSV: Skipping row with missing study code");
                return null;
            }

            log.debug("CSV: Created study: {}", study.getStudyCode());
            return study;

        } catch (Exception e) {
            log.error("CSV: Error creating study from row: {}", Arrays.toString(values), e);
            return null;
        }
    }

    private String cleanHeader(String header) {
        if (header == null) return "";
        return header.trim().toLowerCase()
                .replaceAll("\\s+", " ") // Replace multiple spaces with single space
                .replaceAll("[\\r\\n]", "") // Remove line breaks
                .trim();
    }

    private String cleanValue(String value) {
        if (value == null) return null;

        value = value.trim();

        // Remove quotes if present
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
            value = value.substring(1, value.length() - 1);
            value = value.replace("\"\"", "\""); // Handle escaped quotes
        }

        // Handle various empty value representations
        if (value.isEmpty() ||
                "null".equalsIgnoreCase(value) ||
                "n/a".equalsIgnoreCase(value) ||
                "na".equalsIgnoreCase(value) ||
                "-".equals(value) ||
                "#N/A".equals(value) ||
                "NULL".equals(value)) {
            return null;
        }

        return value.trim();
    }

    private String[] parseCSVLine(String line, char delimiter) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (c == delimiter && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());

        return result.toArray(new String[0]);
    }
}