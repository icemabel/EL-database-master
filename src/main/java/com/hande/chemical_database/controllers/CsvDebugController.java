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
 * CSV Debug Controller
 * This controller helps debug CSV parsing issues
 */
@Controller
@RequestMapping("/api/csv-debug")
@Slf4j
public class CsvDebugController {

    @PostMapping("/analyze")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> analyzeCsv(@RequestParam("file") MultipartFile file) {
        Map<String, Object> analysis = new HashMap<>();

        try {
            log.info("CSV Debug: Analyzing file: {}", file.getOriginalFilename());

            analysis.put("filename", file.getOriginalFilename());
            analysis.put("size", file.getSize());
            analysis.put("contentType", file.getContentType());

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
                List<String> lines = new ArrayList<>();
                String line;
                int lineCount = 0;

                while ((line = reader.readLine()) != null && lineCount < 10) { // Read first 10 lines
                    lines.add(line);
                    lineCount++;
                }

                analysis.put("totalLinesRead", lineCount);
                analysis.put("firstLines", lines);

                if (!lines.isEmpty()) {
                    String firstLine = lines.get(0);
                    analysis.put("firstLine", firstLine);
                    analysis.put("firstLineLength", firstLine.length());

                    // Delimiter analysis
                    Map<String, Object> delimiterAnalysis = analyzeDelimiters(firstLine);
                    analysis.put("delimiterAnalysis", delimiterAnalysis);

                    // Header analysis
                    char detectedDelimiter = detectDelimiter(firstLine);
                    String[] headers = parseCSVLine(firstLine, detectedDelimiter);
                    analysis.put("detectedDelimiter", String.valueOf(detectedDelimiter));
                    analysis.put("headers", Arrays.asList(headers));
                    analysis.put("headerCount", headers.length);

                    // Clean headers analysis
                    List<String> cleanHeaders = new ArrayList<>();
                    for (String header : headers) {
                        cleanHeaders.add(cleanHeader(header));
                    }
                    analysis.put("cleanHeaders", cleanHeaders);

                    // Sample data analysis
                    if (lines.size() > 1) {
                        String sampleDataLine = lines.get(1);
                        String[] sampleData = parseCSVLine(sampleDataLine, detectedDelimiter);
                        analysis.put("sampleDataLine", sampleDataLine);
                        analysis.put("sampleData", Arrays.asList(sampleData));
                        analysis.put("sampleDataCount", sampleData.length);

                        // Clean sample data
                        List<String> cleanSampleData = new ArrayList<>();
                        for (String data : sampleData) {
                            cleanSampleData.add(cleanValue(data));
                        }
                        analysis.put("cleanSampleData", cleanSampleData);
                    }
                }

                analysis.put("status", "success");

            } catch (Exception e) {
                log.error("Error analyzing CSV", e);
                analysis.put("status", "error");
                analysis.put("error", e.getMessage());
            }

            return ResponseEntity.ok(analysis);

        } catch (Exception e) {
            log.error("Error in CSV debug analysis", e);
            analysis.put("status", "error");
            analysis.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(analysis);
        }
    }

    private Map<String, Object> analyzeDelimiters(String line) {
        Map<String, Object> analysis = new HashMap<>();

        int commaCount = 0;
        int semicolonCount = 0;
        int tabCount = 0;
        int pipeCount = 0;
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                switch (c) {
                    case ',': commaCount++; break;
                    case ';': semicolonCount++; break;
                    case '\t': tabCount++; break;
                    case '|': pipeCount++; break;
                }
            }
        }

        analysis.put("commaCount", commaCount);
        analysis.put("semicolonCount", semicolonCount);
        analysis.put("tabCount", tabCount);
        analysis.put("pipeCount", pipeCount);
        analysis.put("recommendedDelimiter", semicolonCount >= commaCount ? ";" : ",");

        return analysis;
    }

    private char detectDelimiter(String line) {
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

        return semicolonCount >= commaCount ? ';' : ',';
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

    private String cleanHeader(String header) {
        if (header == null) return "";
        return header.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[\\r\\n]", "")
                .toLowerCase()
                .trim();
    }

    private String cleanValue(String value) {
        if (value == null) return null;

        value = value.trim();

        if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
            value = value.substring(1, value.length() - 1);
            value = value.replace("\"\"", "\"");
        }

        if (value.isEmpty() ||
                "null".equalsIgnoreCase(value) ||
                "-".equals(value) ||
                "#N/A".equals(value) ||
                "NULL".equals(value)) {
            return null;
        }

        if ("n/a".equalsIgnoreCase(value)) {
            return "N/A";
        }

        return value.trim();
    }

    @GetMapping("/test")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "CSV Debug Controller is working");
        response.put("timestamp", new Date());
        return ResponseEntity.ok(response);
    }
}