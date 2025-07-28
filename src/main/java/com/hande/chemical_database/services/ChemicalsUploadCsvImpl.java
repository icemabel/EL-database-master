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

        // Check for existing chemicals and filter them out
        List<String> existingChemicals = chemicalRepo.findAllChemicalNames();
        Set<Chemicals> newChemicals = chemicalsSet.stream()
                .filter(chemical -> !existingChemicals.contains(chemical.getName()))
                .collect(Collectors.toSet());

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
            char delimiter = ';'; // Default to semicolon for European Excel
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Skip completely empty lines
                if (line.isEmpty()) {
                    continue;
                }

                if (isFirstLine) {
                    // Detect delimiter from first line
                    delimiter = detectDelimiter(line);
                    headers = parseCSVLine(line, delimiter);

                    // Clean headers
                    for (int i = 0; i < headers.length; i++) {
                        headers[i] = cleanHeader(headers[i]);
                    }

                    isFirstLine = false;
                    log.info("CSV: Headers detected: {}", Arrays.toString(headers));
                    log.info("CSV: Using delimiter: '{}'", delimiter);
                    continue;
                }

                if (headers == null) {
                    continue;
                }

                try {
                    // Parse CSV line with detected delimiter
                    String[] values = parseCSVLine(line, delimiter);

                    // Skip lines that are completely empty (all empty values)
                    boolean hasAnyValue = false;
                    for (String value : values) {
                        if (value != null && !value.trim().isEmpty() && !value.equals("\"\"")) {
                            hasAnyValue = true;
                            break;
                        }
                    }

                    if (!hasAnyValue) {
                        log.debug("CSV: Skipping empty line {}", lineNumber);
                        continue;
                    }

                    // Create chemical object from CSV row
                    Chemicals chemical = createChemicalFromCSVRow(headers, values);
                    if (chemical != null) {
                        chemicals.add(chemical);
                    }
                } catch (Exception e) {
                    log.warn("CSV: Error processing line {}: {} - {}", lineNumber, line, e.getMessage());
                    // Continue processing other lines
                }
            }
        }

        log.info("CSV: Processed {} chemicals from file", chemicals.size());
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

    private Chemicals createChemicalFromCSVRow(String[] headers, String[] values) {
        try {
            Chemicals.ChemicalsBuilder builder = Chemicals.builder();

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

                // Map CSV column names
                switch (header) {
                    case "name":
                        builder.name(value);
                        break;
                    case "casno":
                    case "cas no":
                    case "cas_no":
                        builder.CASNo(value);
                        break;
                    case "lotno":
                    case "lot no":
                    case "lot_no":
                        builder.LotNo(value);
                        break;
                    case "producer":
                        builder.producer(value);
                        break;
                    case "storage":
                        builder.storage(value);
                        break;
                    case "toxicstate":
                    case "toxic state":
                    case "toxic_state":
                        if (value != null) {
                            try {
                                // Handle various boolean representations - now includes uppercase
                                String lowerValue = value.toLowerCase();
                                if ("true".equals(lowerValue) || "yes".equals(lowerValue) || "1".equals(lowerValue)) {
                                    builder.toxicState(true);
                                } else if ("false".equals(lowerValue) || "no".equals(lowerValue) || "0".equals(lowerValue)) {
                                    builder.toxicState(false);
                                }
                                // If it's null or unrecognized, leave as null
                            } catch (Exception e) {
                                log.warn("Invalid boolean value for toxicState: {}", value);
                            }
                        }
                        break;
                    case "responsible":
                        builder.responsible(value);
                        break;
                    case "orderdate":
                    case "order date":
                    case "order_date":
                        if (value != null) {
                            try {
                                LocalDate orderDate = parseDate(value);
                                builder.orderDate(orderDate);
                            } catch (Exception e) {
                                log.warn("Invalid date format: {}", value);
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

            Chemicals chemical = builder.build();

            // Validate required fields
            if (chemical.getName() == null || chemical.getName().trim().isEmpty()) {
                log.warn("CSV: Skipping row with missing name");
                return null;
            }

            if (chemical.getStorage() == null || chemical.getStorage().trim().isEmpty()) {
                log.warn("CSV: Skipping row '{}' with missing storage", chemical.getName());
                return null;
            }

            log.debug("CSV: Created chemical: {}", chemical.getName());
            return chemical;

        } catch (Exception e) {
            log.error("CSV: Error creating chemical from row: {}", Arrays.toString(values), e);
            return null;
        }
    }

    private String cleanHeader(String header) {
        if (header == null) return "";
        return header.trim()
                .replaceAll("[\\r\\n]", "") // Remove line breaks
                .replaceAll("\\s+", " ") // Replace multiple spaces with single space
                .toLowerCase()
                .trim();
    }

    private String cleanValue(String value) {
        if (value == null) return null;

        // First remove any carriage returns or newlines
        value = value.replaceAll("[\\r\\n]", "").trim();

        // Remove quotes if present
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
            value = value.substring(1, value.length() - 1);
            value = value.replace("\"\"", "\""); // Handle escaped quotes
        }

        // Handle various empty value representations from Excel
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
        // Updated to prioritize European DD.MM.YYYY format first
        String[] formats = {
                "dd.MM.yyyy",      // European format: 15.01.2024
                "dd/MM/yyyy",      // European format: 15/01/2024
                "dd-MM-yyyy",      // European format: 15-01-2024
                "yyyy-MM-dd",      // ISO format: 2024-01-15
                "MM/dd/yyyy",      // US format: 01/15/2024
                "yyyy/MM/dd",      // Alternative ISO: 2024/01/15
                "MM-dd-yyyy"       // US format: 01-15-2024
        };

        for (String format : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                LocalDate parsedDate = LocalDate.parse(dateString, formatter);
                log.debug("Successfully parsed date '{}' using format '{}'", dateString, format);
                return parsedDate;
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }

        throw new IllegalArgumentException("Unable to parse date: " + dateString + ". Supported formats: DD.MM.YYYY, DD/MM/YYYY, DD-MM-YYYY, YYYY-MM-DD, MM/DD/YYYY");
    }
}