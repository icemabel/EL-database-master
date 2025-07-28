package com.hande.chemical_database.services;

import com.hande.chemical_database.entities.Chemicals;
import com.hande.chemical_database.repositories.ChemicalRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChemicalsUploadCsvImpl implements ChemicalsUploadCsv {

    private final ChemicalRepo chemicalRepo;

    @Override
    public Integer uploadChemicals(MultipartFile file) {
        validateFile(file);

        Set<Chemicals> chemicalsSet;
        try {
            chemicalsSet = parseCsvWithDelimiterDetection(file);
        } catch (IOException e) {
            log.error("Error reading CSV file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Error reading CSV file: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error parsing CSV file: {}", file.getOriginalFilename(), e);
            throw new IllegalArgumentException("Error parsing CSV file: " + e.getMessage(), e);
        }

        if (chemicalsSet.isEmpty()) {
            throw new IllegalArgumentException("No valid chemical records found in CSV file. Check that your file has the correct columns: name, CASNo, LotNo, producer, storage, toxicState, responsible, orderDate, weight");
        }

        log.info("CSV: Parsed {} chemical records from file", chemicalsSet.size());

        // Check for existing chemicals and filter them out
        List<String> existingChemicals = chemicalRepo.findAllChemicalNames();
        Set<String> existingChemicalsSet = new HashSet<>(existingChemicals);

        Set<Chemicals> newChemicals = chemicalsSet.stream()
                .filter(chemical -> !existingChemicalsSet.contains(chemical.getName()))
                .collect(Collectors.toSet());

        log.info("CSV: Found {} new chemicals to save (filtered out {} existing)",
                newChemicals.size(), chemicalsSet.size() - newChemicals.size());

        if (newChemicals.isEmpty()) {
            log.info("All chemicals in CSV already exist in database");
            return 0;
        }

        // Save new chemicals
        List<Chemicals> savedChemicals = chemicalRepo.saveAll(newChemicals);

        int skippedCount = chemicalsSet.size() - newChemicals.size();
        log.info("CSV upload completed: {} new chemicals saved, {} existing chemicals skipped",
                savedChemicals.size(), skippedCount);

        return savedChemicals.size();
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

    private Set<Chemicals> parseCsvWithDelimiterDetection(MultipartFile file) throws IOException {
        Set<Chemicals> chemicals = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            String line;
            boolean isFirstLine = true;
            String[] headers = null;
            char delimiter = ','; // Default to comma first
            int lineNumber = 0;
            int validRecords = 0;
            int skippedRecords = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Remove BOM if present
                if (lineNumber == 1 && line.startsWith("\ufeff")) {
                    line = line.substring(1);
                }

                line = line.trim();

                // Skip completely empty lines
                if (line.isEmpty()) {
                    log.debug("CSV: Skipping empty line {}", lineNumber);
                    continue;
                }

                if (isFirstLine) {
                    // Detect delimiter from first line
                    delimiter = detectDelimiter(line);
                    headers = parseCSVLine(line, delimiter);

                    // Clean and normalize headers
                    for (int i = 0; i < headers.length; i++) {
                        headers[i] = normalizeHeader(headers[i]);
                    }

                    isFirstLine = false;
                    log.info("CSV: Headers detected: {}", Arrays.toString(headers));
                    log.info("CSV: Using delimiter: '{}'", delimiter);

                    // Validate required headers are present
                    if (!hasRequiredHeaders(headers)) {
                        throw new IllegalArgumentException("CSV file is missing required headers. Expected: name, storage (at minimum)");
                    }

                    continue;
                }

                if (headers == null) {
                    continue;
                }

                try {
                    // Parse CSV line with detected delimiter
                    String[] values = parseCSVLine(line, delimiter);

                    // Create chemical object from CSV row
                    Chemicals chemical = createChemicalFromCSVRow(headers, values, lineNumber);
                    if (chemical != null) {
                        chemicals.add(chemical);
                        validRecords++;
                        log.debug("CSV: Successfully parsed chemical: {}", chemical.getName());
                    } else {
                        skippedRecords++;
                    }
                } catch (Exception e) {
                    log.warn("CSV: Error processing line {}: {} - {}", lineNumber, line, e.getMessage());
                    skippedRecords++;
                }
            }

            log.info("CSV: Processing complete - {} valid records, {} skipped records", validRecords, skippedRecords);
        }

        return chemicals;
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

        // Prefer semicolon if present (European format), otherwise comma
        if (semicolonCount > 0) {
            return ';';
        } else {
            return ',';
        }
    }

    private String normalizeHeader(String header) {
        if (header == null) return "";

        return header.trim()
                .replaceAll("[\\r\\n\\t]", "") // Remove all whitespace characters
                .replaceAll("\\s+", "") // Remove all spaces
                .toLowerCase();
    }

    private boolean hasRequiredHeaders(String[] headers) {
        Set<String> headerSet = new HashSet<>(Arrays.asList(headers));
        return headerSet.contains("name") && headerSet.contains("storage");
    }

    private Chemicals createChemicalFromCSVRow(String[] headers, String[] values, int lineNumber) {
        try {
            Chemicals.ChemicalsBuilder builder = Chemicals.builder();

            // Ensure we have enough values, pad with empty strings if necessary
            String[] paddedValues = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                if (i < values.length) {
                    paddedValues[i] = cleanValue(values[i]);
                } else {
                    paddedValues[i] = null;
                }
            }

            // Track which fields we've set
            boolean hasName = false;
            boolean hasStorage = false;

            for (int i = 0; i < headers.length; i++) {
                String header = headers[i];
                String value = paddedValues[i];

                // Map CSV column names with flexible matching
                switch (header) {
                    case "name":
                        if (value != null && !value.trim().isEmpty()) {
                            builder.name(value.trim());
                            hasName = true;
                        }
                        break;
                    case "casno":
                    case "cas_no":
                        builder.casNo(value);  // Updated to use camelCase
                        break;
                    case "lotno":
                    case "lot_no":
                        builder.lotNo(value);  // Updated to use camelCase
                        break;
                    case "producer":
                        builder.producer(value);
                        break;
                    case "storage":
                        if (value != null && !value.trim().isEmpty()) {
                            builder.storage(value.trim());
                            hasStorage = true;
                        }
                        break;
                    case "toxicstate":
                    case "toxic_state":
                        if (value != null) {
                            Boolean toxicState = parseBooleanValue(value);
                            builder.toxicState(toxicState);
                        }
                        break;
                    case "responsible":
                        builder.responsible(value);
                        break;
                    case "orderdate":
                    case "order_date":
                        if (value != null) {
                            LocalDate orderDate = parseDate(value);
                            if (orderDate != null) {
                                builder.orderDate(orderDate);
                            }
                        }
                        break;
                    case "weight":
                        builder.weight(value);
                        break;
                    default:
                        log.debug("CSV: Unmapped column: '{}' with value: '{}'", header, value);
                }
            }

            // Validate required fields
            if (!hasName) {
                log.warn("CSV line {}: Skipping row with missing or empty name", lineNumber);
                return null;
            }

            if (!hasStorage) {
                log.warn("CSV line {}: Skipping row '{}' with missing or empty storage", lineNumber, paddedValues[0]);
                return null;
            }

            Chemicals chemical = builder.build();
            log.debug("CSV line {}: Created chemical: {}", lineNumber, chemical.getName());
            return chemical;

        } catch (Exception e) {
            log.error("CSV line {}: Error creating chemical from row: {} - {}", lineNumber, Arrays.toString(values), e.getMessage());
            return null;
        }
    }

    private String cleanValue(String value) {
        if (value == null) return null;

        // Remove carriage returns, newlines, and trim
        value = value.replaceAll("[\\r\\n]", "").trim();

        // Remove surrounding quotes if present
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
                "#NULL!".equals(value) ||
                "NULL".equals(value) ||
                "#DIV/0!".equals(value)) {
            return null;
        }

        return value.trim();
    }

    private Boolean parseBooleanValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        value = value.trim().toLowerCase();

        switch (value) {
            case "true":
            case "yes":
            case "y":
            case "1":
            case "toxic":
                return true;
            case "false":
            case "no":
            case "n":
            case "0":
            case "safe":
            case "non-toxic":
                return false;
            default:
                log.warn("Unknown boolean value: {}, treating as null", value);
                return null;
        }
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

    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        dateString = dateString.trim();

        // Try common date formats - prioritize European formats first
        String[] formats = {
                "dd.MM.yyyy",    // German/European format (15.01.2024)
                "yyyy-MM-dd",    // ISO format
                "MM/dd/yyyy",    // US format
                "dd/MM/yyyy",    // European format with slashes
                "yyyy/MM/dd",
                "dd-MM-yyyy",
                "MM-dd-yyyy",
                "yyyy.MM.dd"
        };

        for (String format : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                LocalDate parsed = LocalDate.parse(dateString, formatter);
                log.debug("Successfully parsed date '{}' using format '{}'", dateString, format);
                return parsed;
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }

        log.warn("Unable to parse date: {}. Supported formats: dd.MM.yyyy, yyyy-MM-dd, MM/dd/yyyy, etc.", dateString);
        return null; // Return null instead of throwing exception to allow row to be processed
    }
}