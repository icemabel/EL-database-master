package com.hande.chemical_database.controllers;

import com.hande.chemical_database.entities.Chemicals;
import com.hande.chemical_database.models.ChemicalDTO;
import com.hande.chemical_database.services.ChemicalService;
import com.hande.chemical_database.services.ChemicalsFiltering;
import com.hande.chemical_database.services.ChemicalsUploadCsv;
import com.hande.chemical_database.services.QRCodeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.HashMap;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/chemicals")
@RequiredArgsConstructor
@Slf4j
public class ChemicalController {

    private final ChemicalService chemicalService;
    private final ChemicalsFiltering chemicalsFiltering;
    private final ChemicalsUploadCsv chemicalsUploadCsv;
    private final QRCodeService qrCodeService;

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<ChemicalDTO>> getAllChemicals() {
        try {
            log.info("Getting all chemicals...");
            List<ChemicalDTO> chemicals = chemicalService.getAllChemicals();
            log.info("Found {} chemicals", chemicals.size());
            return ResponseEntity.ok(chemicals);
        } catch (Exception e) {
            log.error("Error getting all chemicals", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ChemicalDTO> getChemicalById(@PathVariable Long id) {
        try {
            log.info("Getting chemical by ID: {}", id);
            Optional<ChemicalDTO> chemical = chemicalService.getChemicalById(id);
            return chemical.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting chemical by ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Add this method to your ChemicalController.java class:

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ChemicalDTO> updateChemical(@PathVariable Long id, @Valid @RequestBody ChemicalDTO chemicalDTO) {
        try {
            log.info("Updating chemical with ID: {}", id);

            // Set the ID to ensure we're updating the correct record
            chemicalDTO.setId(id);

            // Check if chemical exists first
            Optional<ChemicalDTO> existingChemical = chemicalService.getChemicalById(id);
            if (existingChemical.isEmpty()) {
                log.warn("Chemical with ID {} not found for update", id);
                return ResponseEntity.notFound().build();
            }

            // Update the chemical using the service
            // You'll need to modify your ChemicalService to support update by ID
            Optional<ChemicalDTO> updatedChemical = chemicalService.updateChemicalById(id, chemicalDTO);

            if (updatedChemical.isPresent()) {
                log.info("Chemical updated successfully with ID: {}", id);
                return ResponseEntity.ok(updatedChemical.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error updating chemical with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<ChemicalDTO> createChemical(@Valid @RequestBody ChemicalDTO chemicalDTO) {
        try {
            log.info("Creating chemical: {}", chemicalDTO.getName());
            ChemicalDTO createdChemical = chemicalService.createChemicals(chemicalDTO);
            log.info("Chemical created with ID: {}", createdChemical.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdChemical);
        } catch (Exception e) {
            log.error("Error creating chemical", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // QR Code endpoints (simplified)
    @PostMapping("/{id}/generate-qr")
    @ResponseBody
    public ResponseEntity<String> generateQRCode(@PathVariable Long id) {
        try {
            log.info("Generating QR code for chemical ID: {}", id);
            String qrCodeId = qrCodeService.generateQRCodeForChemical(id);
            return ResponseEntity.ok("QR code generated successfully. ID: " + qrCodeId);
        } catch (Exception e) {
            log.error("Error generating QR code for chemical ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating QR code: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/qr-image")
    @ResponseBody
    public ResponseEntity<byte[]> getQRCodeImage(@PathVariable Long id) {
        try {
            log.info("Getting QR code image for chemical ID: {}", id);
            byte[] qrCodeImage = qrCodeService.getQRCodeImage(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrCodeImage.length);

            return new ResponseEntity<>(qrCodeImage, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving QR code image for chemical ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // CSV endpoints (simplified)
    @PostMapping("/upload-csv")
    @ResponseBody
    public ResponseEntity<String> uploadChemicalsFromCsv(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Uploading CSV file: {}", file.getOriginalFilename());

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: File is empty");
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest().body("Error: File must be a CSV file");
            }

            Integer uploadedCount = chemicalsUploadCsv.uploadChemicals(file);
            String message = String.format("Successfully uploaded %d chemicals from CSV file", uploadedCount);
            log.info("CSV upload successful: {} chemicals uploaded", uploadedCount);
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            log.error("Error uploading CSV file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing CSV file: " + e.getMessage());
        }
    }

    @GetMapping("/export-csv")
    public void exportChemicalsToCSV(HttpServletResponse response) throws IOException {
        try {
            log.info("Exporting chemicals to CSV");

            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "chemicals_export_" + timestamp + ".csv";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            List<ChemicalDTO> chemicals = chemicalService.getAllChemicals();
            PrintWriter writer = response.getWriter();

            // Write header
            writer.println("name,CASNo,LotNo,producer,storage,toxicState,responsible,orderDate,weight");

            // Write data rows
            for (ChemicalDTO chemical : chemicals) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        escapeCSV(chemical.getName()),
                        escapeCSV(chemical.getCasNo()),
                        escapeCSV(chemical.getLotNo()),
                        escapeCSV(chemical.getProducer()),
                        escapeCSV(chemical.getStorage()),
                        chemical.getToxicState() != null ? chemical.getToxicState().toString() : "",
                        escapeCSV(chemical.getResponsible()),
                        chemical.getOrderDate() != null ? chemical.getOrderDate().toString() : "",
                        escapeCSV(chemical.getWeight())
                );
            }

            writer.flush();
            log.info("Successfully exported {} chemicals to CSV", chemicals.size());

        } catch (Exception e) {
            log.error("Error exporting chemicals to CSV", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error exporting data");
        }
    }

    // Add these methods to your existing ChemicalController.java class

    @DeleteMapping("/clear-all")
    @ResponseBody
    public ResponseEntity<String> clearAllChemicals() {
        try {
            log.info("Clearing all chemicals from database...");

            List<ChemicalDTO> chemicals = chemicalService.getAllChemicals();
            long count = chemicals.size();

            // Delete all chemicals - you'll need to add this method to ChemicalRepo
            // For now, delete one by one:
            for (ChemicalDTO chemical : chemicals) {
                chemicalService.deleteChemical(chemical.getId());
            }

            log.info("Successfully cleared {} chemicals from database", count);
            return ResponseEntity.ok(String.format("Successfully cleared %d chemicals from database", count));

        } catch (Exception e) {
            log.error("Error clearing chemicals", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error clearing chemicals: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteChemical(@PathVariable Long id) {
        try {
            log.info("Deleting chemical with ID: {}", id);

            boolean deleted = chemicalService.deleteChemical(id);

            if (deleted) {
                return ResponseEntity.ok("Chemical deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error deleting chemical with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting chemical: " + e.getMessage());
        }
    }

    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getChemicalCount() {
        try {
            List<ChemicalDTO> chemicals = chemicalService.getAllChemicals();

            Map<String, Object> response = new HashMap<>();
            response.put("total", chemicals.size());
            response.put("toxic", chemicals.stream().filter(c -> Boolean.TRUE.equals(c.getToxicState())).count());
            response.put("safe", chemicals.stream().filter(c -> Boolean.FALSE.equals(c.getToxicState())).count());
            response.put("unknown", chemicals.stream().filter(c -> c.getToxicState() == null).count());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting chemical count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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