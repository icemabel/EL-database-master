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
    public String uploadStudyListsFromCsv(MultipartFile file) {
        Integer count = uploadStudyLists(file);
        return String.format("Successfully uploaded %d studies from CSV file", count);
    }

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
            throw new IllegalArgumentException("No valid study records found in CSV file. Please check the format and column headers.");
        }

        // Get existing study codes to check for duplicates
        Set<String> existingCodes = studyListRepo.findAllStudyCodes()
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // Filter out duplicates and save
        List<StudyList> newStudies = studyListsSet.stream()
                .filter(study -> {
                    String code = study.getStudyCode();
                    if (code == null) return false;
                    return !existingCodes.contains(code.toLowerCase());
                })
                .collect(Collectors.toList());

        if (newStudies.isEmpty()) {
            throw new IllegalArgumentException("All studies in the CSV file already exist in the database.");
        }

        List<StudyList> savedStudies = studyListRepo.saveAll(newStudies);
        log.info("Successfully saved {} studies from CSV file", savedStudies.size());

        return savedStudies.size();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("File must be a CSV file");
        }

        // Check file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size cannot exceed 10MB");
        }
    }

    private Set<StudyList> parseCsvWithDelimiterDetection(MultipartFile file) throws IOException {
        Set<StudyList> studies = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            String line;
            boolean isFirstLine = true;
            String[] headers = null;
            char delimiter = detectDelimiter(file);

            log.info("Detected delimiter: '{}'", delimiter);

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (isFirstLine) {
                    headers = parseCSVLine(line, delimiter);

                    // Clean headers
                    for (int i = 0; i < headers.length; i++) {
                        headers[i] = cleanHeader(headers[i]);
                    }

                    log.info("CSV Headers detected: {}", Arrays.toString(headers));
                    validateHeaders(headers);
                    isFirstLine = false;
                } else {
                    String[] values = parseCSVLine(line, delimiter);
                    StudyList study = createStudyFromCsvRow(headers, values);
                    if (study != null && study.getStudyCode() != null && !study.getStudyCode().trim().isEmpty()) {
                        studies.add(study);
                    }
                }
            }
        }

        log.info("Parsed {} studies from CSV", studies.size());
        return studies;
    }

    private char detectDelimiter(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            String firstLine = reader.readLine();
            if (firstLine == null) return ';';

            // Count occurrences of potential delimiters
            long semicolonCount = firstLine.chars().filter(ch -> ch == ';').count();
            long commaCount = firstLine.chars().filter(ch -> ch == ',').count();
            long tabCount = firstLine.chars().filter(ch -> ch == '\t').count();

            // Use semicolon by default (European standard), then comma, then tab
            if (semicolonCount > 0) return ';';
            if (commaCount > 0) return ',';
            if (tabCount > 0) return '\t';

            return ';'; // Default fallback
        }
    }

    private void validateHeaders(String[] headers) {
        // Required headers (case-insensitive matching)
        Set<String> requiredHeaders = Set.of(
                "study code", "studycode",
                "document codes protocol & report", "document codes", "documentcodes",
                "material type", "materialtype",
                "study level", "studylevel",
                "risk level", "risklevel",
                "responsible person", "responsibleperson"
        );

        Set<String> normalizedHeaders = Arrays.stream(headers)
                .map(this::cleanHeader)
                .collect(Collectors.toSet());

        boolean hasStudyCode = normalizedHeaders.stream()
                .anyMatch(header -> header.contains("study") && header.contains("code"));

        if (!hasStudyCode) {
            throw new IllegalArgumentException(
                    "CSV must contain a 'Study Code' column. Found headers: " + String.join(", ", headers)
            );
        }
    }

    private StudyList createStudyFromCsvRow(String[] headers, String[] values) {
        try {
            StudyList study = new StudyList();

            for (int i = 0; i < headers.length && i < values.length; i++) {
                String header = cleanHeader(headers[i]);
                String value = cleanValue(values[i]);

                switch (header) {
                    case "study code":
                    case "studycode":
                        study.setStudyCode(value);
                        break;
                    case "document codes protocol & report":
                    case "document codes":
                    case "documentcodes":
                        study.setDocumentCodes(value);
                        break;
                    case "material type":
                    case "materialtype":
                        study.setMaterialType(value);
                        break;
                    case "study level":
                    case "studylevel":
                        study.setStudyLevel(value);
                        break;
                    case "risk level":
                    case "risklevel":
                        study.setRiskLevel(value);
                        break;
                    case "info":
                    case "information":
                        study.setInfo(value);
                        break;
                    case "number of samples / sample id":
                    case "number of samples":
                    case "sample id":
                    case "samples":
                    case "numberofsamples":
                        study.setNumberOfSamples(value);
                        break;
                    case "object of study":
                    case "objectofstudy":
                        study.setObjectOfStudy(value);
                        break;
                    case "responsible person":
                    case "responsibleperson":
                        study.setResponsiblePerson(value);
                        break;
                    case "status":
                        study.setStatus(value);
                        break;
                    default:
                        log.debug("Unmapped header: '{}' with value: '{}'", header, value);
                        break;
                }
            }

            // Validate required fields
            if (study.getStudyCode() == null || study.getStudyCode().trim().isEmpty()) {
                log.warn("Skipping row with missing study code: {}", Arrays.toString(values));
                return null;
            }

            // Set default values for empty fields
            if (study.getDocumentCodes() == null) study.setDocumentCodes("protocol / report");
            if (study.getStatus() == null) study.setStatus("pending");

            log.debug("Created study: {} with fields populated", study.getStudyCode());
            return study;

        } catch (Exception e) {
            log.error("Error creating study from row: {}", Arrays.toString(values), e);
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