package com.hande.chemical_database.controllers;

import com.hande.chemical_database.entities.StudyList;
import com.hande.chemical_database.models.StudyListDTO;
import com.hande.chemical_database.services.StudyListService;
import com.hande.chemical_database.services.StudyListUploadCsv;
import com.hande.chemical_database.services.StudyListQRCodeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/studies")
@RequiredArgsConstructor
@Slf4j
public class StudyListController {

    private final StudyListService studyListService;
    private final StudyListUploadCsv studyListUploadCsv;
    private final StudyListQRCodeService qrCodeService;

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<StudyListDTO>> getAllStudyLists() {
        try {
            log.info("Getting all study lists...");
            List<StudyListDTO> studies = studyListService.getAllStudyLists();
            log.info("Found {} studies", studies.size());
            return ResponseEntity.ok(studies);
        } catch (Exception e) {
            log.error("Error getting all studies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<StudyListDTO> getStudyListById(@PathVariable Long id) {
        try {
            log.info("Getting study by ID: {}", id);
            Optional<StudyListDTO> study = studyListService.getStudyListById(id);
            return study.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting study by ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<StudyListDTO> createStudyList(@Valid @RequestBody StudyListDTO studyListDTO) {
        try {
            log.info("Creating study: {}", studyListDTO.getStudyCode());
            StudyListDTO createdStudy = studyListService.createStudyList(studyListDTO);
            log.info("Study created with ID: {}", createdStudy.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStudy);
        } catch (Exception e) {
            log.error("Error creating study", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // QR Code endpoints
    @PostMapping("/{id}/generate-qr")
    @ResponseBody
    public ResponseEntity<String> generateQRCode(@PathVariable Long id) {
        try {
            log.info("Generating QR code for study ID: {}", id);
            String qrCodeId = qrCodeService.generateQRCodeForStudyList(id);
            return ResponseEntity.ok("QR code generated successfully. ID: " + qrCodeId);
        } catch (Exception e) {
            log.error("Error generating QR code for study ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating QR code: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/qr-image")
    @ResponseBody
    public ResponseEntity<byte[]> getQRCodeImage(@PathVariable Long id) {
        try {
            log.info("Getting QR code image for study ID: {}", id);
            byte[] qrCodeImage = qrCodeService.getQRCodeImage(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrCodeImage.length);

            return new ResponseEntity<>(qrCodeImage, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving QR code image for study ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // CSV endpoints
    @PostMapping("/upload-csv")
    @ResponseBody
    public ResponseEntity<String> uploadStudyListsFromCsv(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Uploading CSV file: {}", file.getOriginalFilename());

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Error: File is empty");
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest().body("Error: File must be a CSV file");
            }

            Integer uploadedCount = studyListUploadCsv.uploadStudyLists(file);
            String message = String.format("Successfully uploaded %d studies from CSV file", uploadedCount);
            log.info("CSV upload successful: {} studies uploaded", uploadedCount);
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            log.error("Error uploading CSV file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing CSV file: " + e.getMessage());
        }
    }

    @GetMapping("/export-csv")
    public void exportStudyListsToCSV(HttpServletResponse response) throws IOException {
        try {
            log.info("Exporting studies to CSV");

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
            log.info("Successfully exported {} studies to CSV", studies.size());

        } catch (Exception e) {
            log.error("Error exporting studies to CSV", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error exporting data");
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