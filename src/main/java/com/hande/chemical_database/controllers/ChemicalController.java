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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    // View operations - All authenticated users can view
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'LAB_TECH', 'LAB_USER', 'VIEWER')")
    @ResponseBody
    public ResponseEntity<List<ChemicalDTO>> getAllChemicals() {
        List<ChemicalDTO> chemicals = chemicalService.getAllChemicals();
        return ResponseEntity.ok(chemicals);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'LAB_TECH', 'LAB_USER', 'VIEWER')")
    @ResponseBody
    public ResponseEntity<ChemicalDTO> getChemicalById(@PathVariable Long id) {
        Optional<ChemicalDTO> chemical = chemicalService.getChemicalById(id);
        return chemical.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'LAB_TECH', 'LAB_USER', 'VIEWER')")
    @ResponseBody
    public ResponseEntity<ChemicalDTO> searchByName(@RequestParam String name) {
        Optional<ChemicalDTO> chemical = chemicalsFiltering.searchByName(name);
        return chemical.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search-cas")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'LAB_TECH', 'LAB_USER', 'VIEWER')")
    @ResponseBody
    public ResponseEntity<List<Chemicals>> searchByCAS(@RequestParam String casNo) {
        List<Chemicals> chemicals = chemicalsFiltering.searchByCASNo(casNo);
        return ResponseEntity.ok(chemicals);
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'LAB_TECH', 'LAB_USER', 'VIEWER')")
    @ResponseBody
    public ResponseEntity<Page<Chemicals>> filterChemicals(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String storage,
            @RequestParam(required = false) String responsible,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "25") Integer size) {

        PageRequest pageRequest = chemicalsFiltering.buildPageRequest(page, size);
        Chemicals filterCriteria = Chemicals.builder()
                .name(name)
                .storage(storage)
                .responsible(responsible)
                .build();

        Page<Chemicals> result;
        if (name != null && !name.trim().isEmpty()) {
            result = chemicalsFiltering.listChemicalsByName(filterCriteria, pageRequest);
        } else if (storage != null && !storage.trim().isEmpty()) {
            result = chemicalsFiltering.listChemicalsByStorage(filterCriteria, pageRequest);
        } else if (responsible != null && !responsible.trim().isEmpty()) {
            result = chemicalsFiltering.listChemicalsByOwner(filterCriteria, pageRequest);
        } else {
            result = chemicalsFiltering.listChemicalsByName(filterCriteria, pageRequest);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/toxic")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'LAB_TECH', 'LAB_USER', 'VIEWER')")
    @ResponseBody
    public ResponseEntity<Page<Chemicals>> getToxicChemicals(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "25") Integer size) {

        PageRequest pageRequest = chemicalsFiltering.buildPageRequest(page, size);
        Chemicals filterCriteria = Chemicals.builder().build();
        Page<Chemicals> toxicChemicals = chemicalsFiltering.showToxicChemicals(filterCriteria, pageRequest);

        return ResponseEntity.ok(toxicChemicals);
    }

    // Create operations - All except viewers
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'LAB_TECH', 'LAB_USER')")
    @ResponseBody
    public ResponseEntity<ChemicalDTO> createChemical(@Valid @RequestBody ChemicalDTO chemicalDTO) {
        try {
            ChemicalDTO createdChemical = chemicalService.createChemicals(chemicalDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdChemical);
        } catch (Exception e) {
            log.error("Error creating chemical: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Update operations - All except viewers
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'LAB_TECH', 'LAB_USER')")
    @ResponseBody
    public ResponseEntity<ChemicalDTO> updateChemical(@Valid @RequestBody ChemicalDTO chemicalDTO) {
        try {
            Optional<ChemicalDTO> updatedChemical = chemicalService.updateChemicals(chemicalDTO);
            return updatedChemical.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error updating chemical: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Delete operations - Only Admin and Lab Manager
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER')")
    @ResponseBody
    public ResponseEntity<Void> deleteChemical(@PathVariable Long id) {
        try {
            boolean deleted = chemicalService.deleteChemical(id);
            return deleted ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting chemical: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/name/{name}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER')")
    @ResponseBody
    public ResponseEntity<Void> deleteChemicalByName(@PathVariable String name) {
        try {
            boolean deleted = chemicalService.deleteChemicalByName(name);
            return deleted ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting chemical by name: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // CSV operations - Lab Tech level and above
    @PostMapping("/upload-csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'LAB_TECH')")
    @ResponseBody
    public ResponseEntity<String> uploadChemicalsFromCsv(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: File is empty");
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest().body("Error: File must be a CSV file");
            }

            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("Error: File size exceeds 10MB limit");
            }

            Integer uploadedCount = chemicalsUploadCsv.uploadChemicals(file);
            String message = String.format("Successfully uploaded %d chemicals from CSV file", uploadedCount);
            log.info("CSV upload successful: {} chemicals uploaded from file: {}", uploadedCount, file.getOriginalFilename());
            return ResponseEntity.ok(message);

        } catch (IllegalArgumentException e) {
            log.error("CSV upload validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error uploading CSV file: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing CSV file: " + e.getMessage());
        }
    }

    @GetMapping("/export-csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'LAB_TECH', 'LAB_USER')")
    public void exportChemicalsToCSV(HttpServletResponse response) throws IOException {
        try {
            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "chemicals_export_" + timestamp + ".csv";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            List<ChemicalDTO> chemicals = chemicalService.getAllChemicals();
            PrintWriter writer = response.getWriter();

            writer.println("name,CASNo,LotNo,producer,storage,toxicState,responsible,orderDate,weight");

            for (ChemicalDTO chemical : chemicals) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        escapeCSV(chemical.getName()),
                        escapeCSV(chemical.getCASNo()),
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

    // QR Code operations - All authenticated users except viewers for generation
    @PostMapping("/{id}/generate-qr")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'LAB_TECH', 'LAB_USER')")
    @ResponseBody
    public ResponseEntity<String> generateQRCode(@PathVariable Long id) {
        try {
            String qrCodeId = qrCodeService.generateQRCodeForChemical(id);
            return ResponseEntity.ok("QR code generated successfully. ID: " + qrCodeId);
        } catch (Exception e) {
            log.error("Error generating QR code for chemical ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating QR code: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/qr-image")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'LAB_TECH', 'LAB_USER', 'VIEWER')")
    @ResponseBody
    public ResponseEntity<byte[]> getQRCodeImage(@PathVariable Long id) {
        try {
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

    @PostMapping("/{id}/regenerate-qr")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_MANAGER', 'LAB_TECH')")
    @ResponseBody
    public ResponseEntity<String> regenerateQRCode(@PathVariable Long id) {
        try {
            qrCodeService.regenerateQRCode(id);
            return ResponseEntity.ok("QR code regenerated successfully");
        } catch (Exception e) {
            log.error("Error regenerating QR code for chemical ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error regenerating QR code: " + e.getMessage());
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