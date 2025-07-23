package com.hande.chemical_database.services;

import com.hande.chemical_database.entities.Chemicals;
import com.hande.chemical_database.repositories.ChemicalRepo;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
            chemicalsSet = parseCsv(file);
        } catch (IOException e) {
            log.error("Error reading CSV file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Error reading CSV file: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error parsing CSV file: {}", file.getOriginalFilename(), e);
            throw new IllegalArgumentException("Error parsing CSV file: " + e.getMessage(), e);
        }

        if (chemicalsSet.isEmpty()) {
            throw new IllegalArgumentException("No valid chemical records found in CSV file");
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

    private Set<Chemicals> parseCsv(MultipartFile file) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            HeaderColumnNameMappingStrategy<ChemicalCsvRecord> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(ChemicalCsvRecord.class);

            CsvToBean<ChemicalCsvRecord> csvToBean = new CsvToBeanBuilder<ChemicalCsvRecord>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreEmptyLine(true)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withThrowExceptions(false) // Don't throw exceptions for malformed lines
                    .build();

            List<ChemicalCsvRecord> csvRecords = csvToBean.parse();

            // Check for parsing exceptions
            List<CsvException> capturedExceptions = csvToBean.getCapturedExceptions();
            if (!capturedExceptions.isEmpty()) {
                log.warn("Found {} parsing errors in CSV file", capturedExceptions.size());
                for (CsvException exception : capturedExceptions) {
                    log.warn("CSV parsing error at line {}: {}", exception.getLineNumber(), exception.getMessage());
                }
            }

            // Filter duplicates and validate records
            Set<String> seenChemicals = new HashSet<>();
            return csvRecords.stream()
                    .filter(this::validateCsvRecord)
                    .map(this::convertCsvRecordToChemical)
                    .filter(chemical -> chemical != null && seenChemicals.add(chemical.getName()))
                    .collect(Collectors.toSet());
        }
    }

    private boolean validateCsvRecord(ChemicalCsvRecord record) {
        if (record == null) {
            log.warn("Null CSV record found, skipping");
            return false;
        }

        if (record.getName() == null || record.getName().trim().isEmpty()) {
            log.warn("CSV record missing required 'name' field, skipping");
            return false;
        }

        if (record.getStorage() == null || record.getStorage().trim().isEmpty()) {
            log.warn("CSV record '{}' missing required 'storage' field, skipping", record.getName());
            return false;
        }

        return true;
    }

    private Chemicals convertCsvRecordToChemical(ChemicalCsvRecord csvRecord) {
        try {
            Chemicals.ChemicalsBuilder builder = Chemicals.builder()
                    .name(csvRecord.getName().trim())
                    .CASNo(trimOrNull(csvRecord.getCASNo()))
                    .LotNo(trimOrNull(csvRecord.getLotNo()))
                    .producer(trimOrNull(csvRecord.getProducer()))
                    .storage(csvRecord.getStorage().trim())
                    .responsible(trimOrNull(csvRecord.getResponsible()))
                    .weight(trimOrNull(csvRecord.getWeight()));

            // Handle toxic state
            if (csvRecord.getToxicState() != null) {
                builder.toxicState(csvRecord.getToxicState());
            }

            // Handle order date with flexible parsing
            if (csvRecord.getOrderDate() != null && !csvRecord.getOrderDate().trim().isEmpty()) {
                try {
                    LocalDate orderDate = parseDate(csvRecord.getOrderDate().trim());
                    builder.orderDate(orderDate);
                } catch (Exception e) {
                    log.warn("Invalid date format for chemical '{}': {}", csvRecord.getName(), csvRecord.getOrderDate());
                }
            }

            return builder.build();

        } catch (Exception e) {
            log.error("Error converting CSV record to Chemical: {}", csvRecord, e);
            return null;
        }
    }

    private String trimOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private LocalDate parseDate(String dateString) {
        // Try common date formats
        String[] formats = {
                "yyyy-MM-dd",
                "MM/dd/yyyy",
                "dd/MM/yyyy",
                "yyyy/MM/dd",
                "dd-MM-yyyy",
                "MM-dd-yyyy"
        };

        for (String format : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }

        throw new IllegalArgumentException("Unable to parse date: " + dateString);
    }

    // Inner class for CSV mapping
    public static class ChemicalCsvRecord {
        private String name;
        private String CASNo;
        private String LotNo;
        private String producer;
        private String storage;
        private Boolean toxicState;
        private String responsible;
        private String orderDate;
        private String weight;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getCASNo() { return CASNo; }
        public void setCASNo(String CASNo) { this.CASNo = CASNo; }

        public String getLotNo() { return LotNo; }
        public void setLotNo(String LotNo) { this.LotNo = LotNo; }

        public String getProducer() { return producer; }
        public void setProducer(String producer) { this.producer = producer; }

        public String getStorage() { return storage; }
        public void setStorage(String storage) { this.storage = storage; }

        public Boolean getToxicState() { return toxicState; }
        public void setToxicState(Boolean toxicState) { this.toxicState = toxicState; }

        public String getResponsible() { return responsible; }
        public void setResponsible(String responsible) { this.responsible = responsible; }

        public String getOrderDate() { return orderDate; }
        public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

        public String getWeight() { return weight; }
        public void setWeight(String weight) { this.weight = weight; }

        @Override
        public String toString() {
            return "ChemicalCsvRecord{" +
                    "name='" + name + '\'' +
                    ", CASNo='" + CASNo + '\'' +
                    ", storage='" + storage + '\'' +
                    '}';
        }
    }
}