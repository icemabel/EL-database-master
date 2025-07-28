package com.hande.chemical_database.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * In-Memory StudyList API Controller
 * This controller provides API endpoints for studies using in-memory storage
 * Use this when you don't have database services set up yet
 */
@Controller
@RequestMapping("/api/studies-memory")  // CHANGED: Different base path to avoid conflicts
@Slf4j
public class StudyListInMemoryController {

    // In-memory storage for studies
    private static final List<Map<String, Object>> allStudies = new ArrayList<>();

    static {
        // Initialize with sample data
        initializeSampleData();
    }

    private static void initializeSampleData() {
        Map<String, Object> study1 = new HashMap<>();
        study1.put("id", 1L);
        study1.put("studyCode", "CMQ_STD_001");
        study1.put("documentCodes", "protocol / report");
        study1.put("materialType", "tubing");
        study1.put("studyLevel", "A");
        study1.put("riskLevel", "Low");
        study1.put("info", "official study no: 155STD058_00");
        study1.put("numberOfSamples", null);
        study1.put("objectOfStudy", "Pharmline tube");
        study1.put("responsiblePerson", "Sabine");
        study1.put("status", "completed");
        allStudies.add(study1);

        Map<String, Object> study2 = new HashMap<>();
        study2.put("id", 2L);
        study2.put("studyCode", "CMQ_STD_002");
        study2.put("documentCodes", "protocol / report");
        study2.put("materialType", "container");
        study2.put("studyLevel", "B");
        study2.put("riskLevel", "Medium");
        study2.put("info", "official study no: 155STD059_00");
        study2.put("numberOfSamples", "S001, S002, S003");
        study2.put("objectOfStudy", "IV bag material");
        study2.put("responsiblePerson", "John");
        study2.put("status", "in progress");
        allStudies.add(study2);

        Map<String, Object> study3 = new HashMap<>();
        study3.put("id", 3L);
        study3.put("studyCode", "CMQ_STD_003");
        study3.put("documentCodes", "protocol / report");
        study3.put("materialType", "closure");
        study3.put("studyLevel", "A");
        study3.put("riskLevel", "High");
        study3.put("info", "official study no: 155STD060_00");
        study3.put("numberOfSamples", "S004, S005, S006");
        study3.put("objectOfStudy", "Rubber stopper");
        study3.put("responsiblePerson", "Maria");
        study3.put("status", "pending");
        allStudies.add(study3);

        log.info("Initialized in-memory storage with {} sample studies", allStudies.size());
    }

    // ===== API ENDPOINTS =====
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAllStudies() {
        try {
            log.info("API: Getting all studies - found {} studies", allStudies.size());
            return ResponseEntity.ok(allStudies);
        } catch (Exception e) {
            log.error("API: Error getting studies", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStudyById(@PathVariable Long id) {
        try {
            log.info("API: Getting study by ID: {}", id);
            Optional<Map<String, Object>> study = allStudies.stream()
                    .filter(s -> Objects.equals(s.get("id"), id))
                    .findFirst();

            if (study.isPresent()) {
                return ResponseEntity.ok(study.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("API: Error getting study by ID: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ===== CSV OPERATIONS =====
    @PostMapping("/upload-csv")
    @ResponseBody
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            log.info("CSV: Upload request received: {}", file.getOriginalFilename());

            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: File is empty");
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest().body("Error: File must be a CSV file");
            }

            // Process CSV file with automatic delimiter detection
            List<Map<String, Object>> newStudies = processCsvFileWithDelimiterDetection(file);

            if (newStudies.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: No valid studies found in CSV file. Check that your file has the correct columns: Study Code, Document codes protocol & report, Material type, Study Level, Risk Level, Info, Number of Samples / Sample ID, Object of study, Responsible Person, Status");
            }

            // Add new studies to existing list (avoiding duplicates)
            int addedCount = 0;
            Set<String> existingCodes = new HashSet<>();
            for (Map<String, Object> study : allStudies) {
                existingCodes.add((String) study.get("studyCode"));
            }

            Long nextId = (long) (allStudies.size() + 1);
            for (Map<String, Object> newStudy : newStudies) {
                String studyCode = (String) newStudy.get("studyCode");
                if (studyCode != null && !existingCodes.contains(studyCode)) {
                    newStudy.put("id", nextId++);
                    allStudies.add(newStudy);
                    addedCount++;
                    log.info("CSV: Added study: {}", studyCode);
                } else if (studyCode != null) {
                    log.info("CSV: Skipped duplicate study: {}", studyCode);
                }
            }

            String message = String.format("Successfully processed CSV file. Added %d new studies, skipped %d duplicates. Total studies: %d",
                    addedCount, newStudies.size() - addedCount, allStudies.size());

            log.info("CSV: Upload completed: {}", message);
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            log.error("CSV: Error uploading file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.internalServerError()
                    .body("Error processing CSV file: " + e.getMessage() + ". Make sure your CSV file has the correct format with either comma (,) or semicolon (;) separators.");
        }
    }

    @GetMapping("/export-csv")
    public void exportCsv(jakarta.servlet.http.HttpServletResponse response) {
        try {
            log.info("CSV: Export request received");

            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");

            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "studies_export_" + timestamp + ".csv";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            try (java.io.PrintWriter writer = response.getWriter()) {
                // Write CSV header (using semicolons for European Excel compatibility)
                writer.println("Study Code;Document codes protocol & report;Material type;Study Level;Risk Level;Info;Number of Samples / Sample ID;Object of study;Responsible Person;Status");

                // Write data rows
                for (Map<String, Object> study : allStudies) {
                    writer.printf("\"%s\";\"%s\";\"%s\";\"%s\";\"%s\";\"%s\";\"%s\";\"%s\";\"%s\";\"%s\"%n",
                            escapeCSV((String) study.get("studyCode")),
                            escapeCSV((String) study.get("documentCodes")),
                            escapeCSV((String) study.get("materialType")),
                            escapeCSV((String) study.get("studyLevel")),
                            escapeCSV((String) study.get("riskLevel")),
                            escapeCSV((String) study.get("info")),
                            escapeCSV((String) study.get("numberOfSamples")),
                            escapeCSV((String) study.get("objectOfStudy")),
                            escapeCSV((String) study.get("responsiblePerson")),
                            escapeCSV((String) study.get("status"))
                    );
                }
                writer.flush();
            }

            log.info("CSV: Successfully exported {} studies with semicolon separators", allStudies.size());

        } catch (Exception e) {
            log.error("CSV: Error exporting", e);
            try {
                response.sendError(500, "Export failed: " + e.getMessage());
            } catch (Exception ex) {
                log.error("CSV: Error sending error response", ex);
            }
        }
    }

    // ===== TEST ENDPOINT =====
    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "StudyList In-Memory API is working");
        response.put("studyCount", allStudies.size());
        response.put("timestamp", new java.util.Date());
        response.put("features", Arrays.asList("Semicolon CSV support", "Automatic delimiter detection", "Excel compatibility", "In-memory storage"));
        return ResponseEntity.ok(response);
    }

    // ===== PRIVATE HELPER METHODS =====
    private List<Map<String, Object>> processCsvFileWithDelimiterDetection(MultipartFile file) throws Exception {
        List<Map<String, Object>> studies = new ArrayList<>();

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
                Map<String, Object> study = createStudyFromCSVRow(headers, values);
                if (study != null) {
                    studies.add(study);
                }
            }
        }

        log.info("CSV: Processed {} studies from file", studies.size());
        return studies;
    }

    private char detectDelimiter(String line) {
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
        return semicolonCount >= commaCount ? ';' : ',';
    }

    private Map<String, Object> createStudyFromCSVRow(String[] headers, String[] values) {
        try {
            Map<String, Object> study = new HashMap<>();

            for (int i = 0; i < Math.min(headers.length, values.length); i++) {
                String header = headers[i].trim().toLowerCase();
                String value = values[i].trim();

                // Remove quotes if present
                if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
                    value = value.substring(1, value.length() - 1);
                }

                // Handle empty values
                if (value.isEmpty() || "null".equalsIgnoreCase(value)) {
                    value = null;
                }

                // Map Excel column names exactly as they appear
                switch (header) {
                    case "study code":
                        study.put("studyCode", value);
                        break;
                    case "document codes protocol & report":
                    case "document codes protocol & report ": // Handle trailing space
                        study.put("documentCodes", value);
                        break;
                    case "material type":
                        study.put("materialType", value);
                        break;
                    case "study level":
                        study.put("studyLevel", value);
                        break;
                    case "risk level":
                    case "risk level ": // Handle trailing space
                        study.put("riskLevel", value);
                        break;
                    case "info":
                        study.put("info", value);
                        break;
                    case "number of samples / sample id":
                        study.put("numberOfSamples", value);
                        break;
                    case "object of study":
                        study.put("objectOfStudy", value);
                        break;
                    case "responsible person":
                        study.put("responsiblePerson", value);
                        break;
                    case "status":
                        study.put("status", value);
                        break;
                    default:
                        log.debug("CSV: Unmapped column: '{}' with value: '{}'", header, value);
                }
            }

            // Validate required fields
            if (study.get("studyCode") == null || study.get("studyCode").toString().trim().isEmpty()) {
                log.warn("CSV: Skipping row with missing study code. Available fields: {}", study.keySet());
                return null;
            }

            log.debug("CSV: Created study: {} with fields: {}", study.get("studyCode"), study.keySet());
            return study;

        } catch (Exception e) {
            log.error("CSV: Error creating study from row: {}", Arrays.toString(values), e);
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

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains("\"")) {
            value = value.replace("\"", "\"\"");
        }
        return value;
    }
}