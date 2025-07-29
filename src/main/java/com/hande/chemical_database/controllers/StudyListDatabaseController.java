package com.hande.chemical_database.controllers;

import com.hande.chemical_database.entities.StudyList;
import com.hande.chemical_database.models.StudyListDTO;
import com.hande.chemical_database.services.StudyListService;
import com.hande.chemical_database.services.StudyListUploadCsv;
import com.hande.chemical_database.services.StudyListQRCodeService;
import org.springframework.data.domain.Page;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * StudyList Database Controller
 * This controller uses actual database services (StudyListService, etc.)
 * Use this when you have proper database configuration set up
 *
 * If you get bean injection errors, use StudyListInMemoryController instead
 */
@Controller
@RequestMapping("/api/studies-db")
@RequiredArgsConstructor
@Slf4j
public class StudyListDatabaseController {

    private final StudyListService studyListService;
    private final StudyListUploadCsv studyListUploadCsv;
    private final StudyListQRCodeService qrCodeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<List<StudyListDTO>> getAllStudyLists() {
        try {
            log.info("Database: Getting all study lists...");
            List<StudyListDTO> studies = studyListService.getAllStudyLists();
            log.info("Database: Found {} studies", studies.size());
            return ResponseEntity.ok(studies);
        } catch (Exception e) {
            log.error("Database: Error getting all studies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<StudyListDTO> getStudyListById(@PathVariable Long id) {
        try {
            log.info("Database: Getting study by ID: {}", id);
            Optional<StudyListDTO> study = studyListService.getStudyListById(id);
            return study.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Database: Error getting study by ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<StudyListDTO> createStudyList(@Valid @RequestBody StudyListDTO studyListDTO) {
        try {
            log.info("Database: Creating study: {}", studyListDTO.getStudyCode());
            StudyListDTO createdStudy = studyListService.createStudyList(studyListDTO);
            log.info("Database: Study created with ID: {}", createdStudy.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStudy);
        } catch (Exception e) {
            log.error("Database: Error creating study", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<StudyListDTO> updateStudyList(@PathVariable Long id, @Valid @RequestBody StudyListDTO studyListDTO) {
        try {
            log.info("Database: Updating study with ID: {}", id);
            studyListDTO.setId(id); // Ensure the ID is set for update
            Optional<StudyListDTO> updatedStudy = studyListService.updateStudyList(studyListDTO);
            return updatedStudy.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Database: Error updating study with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Void> deleteStudyList(@PathVariable Long id) {
        try {
            log.info("Database: Deleting study with ID: {}", id);
            boolean deleted = studyListService.deleteStudyList(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Database: Error deleting study with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // QR Code endpoints
    @PostMapping("/{id}/generate-qr")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<String> generateQRCode(@PathVariable Long id) {
        try {
            log.info("Database: Generating QR code for study ID: {}", id);
            String qrCodeId = qrCodeService.generateQRCodeForStudyList(id);
            return ResponseEntity.ok("QR code generated successfully. ID: " + qrCodeId);
        } catch (Exception e) {
            log.error("Database: Error generating QR code for study ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating QR code: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/qr-image")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<byte[]> getQRCodeImage(@PathVariable Long id) {
        try {
            log.info("Database: Getting QR code image for study ID: {}", id);
            byte[] qrCodeImage = qrCodeService.getQRCodeImage(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrCodeImage.length);

            return new ResponseEntity<>(qrCodeImage, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Database: Error retrieving QR code image for study ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{id}/regenerate-qr")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<String> regenerateQRCode(@PathVariable Long id) {
        try {
            log.info("Database: Regenerating QR code for study ID: {}", id);
            qrCodeService.regenerateQRCode(id);
            return ResponseEntity.ok("QR code regenerated successfully");
        } catch (Exception e) {
            log.error("Database: Error regenerating QR code for study ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error regenerating QR code: " + e.getMessage());
        }
    }

    // CSV endpoints
    @PostMapping("/upload-csv")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<String> uploadStudyListsFromCsv(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Database: Uploading CSV file: {}", file.getOriginalFilename());

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: File is empty");
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest().body("Error: File must be a CSV file");
            }

            Integer uploadedCount = studyListUploadCsv.uploadStudyLists(file);
            String message = String.format("Successfully uploaded %d studies from CSV file", uploadedCount);
            log.info("Database: CSV upload successful: {} studies uploaded", uploadedCount);
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            log.error("Database: Error uploading CSV file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing CSV file: " + e.getMessage());
        }
    }

    @GetMapping("/export-csv")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void exportStudyListsToCSV(HttpServletResponse response) throws IOException {
        try {
            log.info("Database: Exporting studies to CSV");

            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "studies_export_" + timestamp + ".csv";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            List<StudyListDTO> studies = studyListService.getAllStudyLists();
            PrintWriter writer = response.getWriter();

            // Write header
            writer.println("studyCode,documentCodes,materialType,studyLevel,riskLevel,info,numberOfSamples,objectOfStudy,responsiblePerson,status");

            // Write data rows
            for (StudyListDTO study : studies) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        escapeCSV(study.getStudyCode()),
                        escapeCSV(study.getDocumentCodes()),
                        escapeCSV(study.getMaterialType()),
                        escapeCSV(study.getStudyLevel()),
                        escapeCSV(study.getRiskLevel()),
                        escapeCSV(study.getInfo()),
                        escapeCSV(study.getNumberOfSamples()),
                        escapeCSV(study.getObjectOfStudy()),
                        escapeCSV(study.getResponsiblePerson()),
                        escapeCSV(study.getStatus())
                );
            }

            writer.flush();
            log.info("Database: Successfully exported {} studies to CSV", studies.size());

        } catch (Exception e) {
            log.error("Database: Error exporting studies to CSV", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error exporting data");
        }
    }

    // Search endpoints
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<List<StudyListDTO>> searchStudyListsByCode(@RequestParam String studyCode) {
        try {
            log.info("Database: Searching studies by code: {}", studyCode);

            // Using the pageable method from service with default pagination
            org.springframework.data.domain.PageRequest pageRequest =
                    org.springframework.data.domain.PageRequest.of(0, 100);

            Page<StudyList> studyPage = studyListService.listStudyListsByCode(studyCode, pageRequest);

            // Convert entities to DTOs (you'll need a mapper for this)
            List<StudyListDTO> studies = studyPage.getContent().stream()
                    .map(this::convertToDTO)
                    .collect(java.util.stream.Collectors.toList());

            log.info("Database: Found {} studies matching search criteria", studies.size());
            return ResponseEntity.ok(studies);
        } catch (Exception e) {
            log.error("Database: Error searching studies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/by-code/{studyCode}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Void> deleteStudyListByCode(@PathVariable String studyCode) {
        try {
            log.info("Database: Deleting study by code: {}", studyCode);
            boolean deleted = studyListService.deleteStudyListByCode(studyCode);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Database: Error deleting study by code: {}", studyCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper method to convert Entity to DTO (implement this based on your mapping strategy)
    private StudyListDTO convertToDTO(StudyList studyList) {
        return StudyListDTO.builder()
                .id(studyList.getId())
                .studyCode(studyList.getStudyCode())
                .documentCodes(studyList.getDocumentCodes())
                .materialType(studyList.getMaterialType())
                .studyLevel(studyList.getStudyLevel())
                .riskLevel(studyList.getRiskLevel())
                .info(studyList.getInfo())
                .numberOfSamples(studyList.getNumberOfSamples())
                .objectOfStudy(studyList.getObjectOfStudy())
                .responsiblePerson(studyList.getResponsiblePerson())
                .status(studyList.getStatus())
                .qrCode(studyList.getQrCode())
                .qrCodeGeneratedAt(studyList.getQrCodeGeneratedAt())
                .build();
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