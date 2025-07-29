package com.hande.chemical_database.controllers;

import com.hande.chemical_database.entities.StudyList;
import com.hande.chemical_database.models.StudyListDTO;
import com.hande.chemical_database.models.UserPrincipal;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

/**
 * StudyList Controller with Database Persistence
 * This controller uses actual database services and persistence
 * Unified endpoint at /api/studies for consistency with chemicals (/api/chemicals)
 */
@Controller
@RequestMapping("/api/studies")  // Changed from /api/studies-db to /api/studies
@RequiredArgsConstructor
@Slf4j
public class StudyListController {

    private final StudyListService studyListService;
    private final StudyListUploadCsv studyListUploadCsv;
    private final StudyListQRCodeService qrCodeService;

    // ===== READ OPERATIONS =====
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<StudyListDTO> getStudyListById(@PathVariable Long id) {
        try {
            log.info("Getting study by ID: {}", id);
            Optional<StudyListDTO> study = studyListService.getStudyListById(id);
            return study.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting study by ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStudyCount() {
        try {
            List<StudyListDTO> studies = studyListService.getAllStudyLists();

            Map<String, Object> response = new HashMap<>();
            response.put("total", studies.size());
            response.put("completed", studies.stream().filter(s -> "completed".equalsIgnoreCase(s.getStatus())).count());
            response.put("inProgress", studies.stream().filter(s -> "in progress".equalsIgnoreCase(s.getStatus())).count());
            response.put("pending", studies.stream().filter(s -> "pending".equalsIgnoreCase(s.getStatus())).count());
            response.put("highRisk", studies.stream().filter(s -> "High".equalsIgnoreCase(s.getRiskLevel())).count());
            response.put("mediumRisk", studies.stream().filter(s -> "Medium".equalsIgnoreCase(s.getRiskLevel())).count());
            response.put("lowRisk", studies.stream().filter(s -> "Low".equalsIgnoreCase(s.getRiskLevel())).count());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting study count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== CREATE OPERATIONS =====
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<StudyListDTO> createStudyList(@Valid @RequestBody StudyListDTO studyListDTO) {
        try {
            log.info("Creating new study with code: {}", studyListDTO.getStudyCode());
            StudyListDTO createdStudy = studyListService.createStudyList(studyListDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStudy);
        } catch (Exception e) {
            log.error("Error creating study", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== UPDATE OPERATIONS =====
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<StudyListDTO> updateStudyList(@PathVariable Long id, @Valid @RequestBody StudyListDTO studyListDTO) {
        try {
            log.info("Updating study with ID: {}", id);
            studyListDTO.setId(id);
            Optional<StudyListDTO> updatedStudy = studyListService.updateStudyList(studyListDTO);
            return updatedStudy.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error updating study with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== DELETE OPERATIONS =====
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<String> deleteStudyList(@PathVariable Long id) {
        try {
            log.info("Deleting study with ID: {}", id);
            boolean deleted = studyListService.deleteStudyList(id);
            if (deleted) {
                return ResponseEntity.ok("Study deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting study with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting study: " + e.getMessage());
        }
    }

    @DeleteMapping("/by-code/{studyCode}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<String> deleteStudyListByCode(@PathVariable String studyCode) {
        try {
            log.info("Deleting study by code: {}", studyCode);
            boolean deleted = studyListService.deleteStudyListByCode(studyCode);
            if (deleted) {
                return ResponseEntity.ok("Study deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting study by code: {}", studyCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting study: " + e.getMessage());
        }
    }

    @DeleteMapping("/clear-all")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<String> clearAllStudyLists() {
        try {
            log.info("Clearing all studies from database...");

            List<StudyListDTO> studies = studyListService.getAllStudyLists();
            long count = studies.size();

            // Delete all studies one by one
            for (StudyListDTO study : studies) {
                studyListService.deleteStudyList(study.getId());
            }

            log.info("Successfully cleared {} studies from database", count);
            return ResponseEntity.ok(String.format("Successfully cleared %d studies from database", count));

        } catch (Exception e) {
            log.error("Error clearing studies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error clearing studies: " + e.getMessage());
        }
    }

    // ===== CSV IMPORT/EXPORT =====
    @PostMapping("/import-csv")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Importing CSV file: {}", file.getOriginalFilename());

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            String result = studyListUploadCsv.uploadStudyListsFromCsv(file);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error importing CSV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error importing CSV: " + e.getMessage());
        }
    }

    @PostMapping("/upload-csv")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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

            String result = studyListUploadCsv.uploadStudyListsFromCsv(file);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error uploading CSV file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing CSV file: " + e.getMessage());
        }
    }

    @GetMapping("/export-csv")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void exportCsv(HttpServletResponse response) {
        try {
            log.info("Exporting studies to CSV");

            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "studies_export_" + timestamp + ".csv";
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

            List<StudyListDTO> studies = studyListService.getAllStudyLists();
            PrintWriter writer = response.getWriter();

            // Write header with semicolon separators for European Excel compatibility
            writer.println("studyCode;documentCodes;materialType;studyLevel;riskLevel;info;numberOfSamples;objectOfStudy;responsiblePerson;status");

            // Write data rows
            for (StudyListDTO study : studies) {
                writer.printf("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s%n",
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
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error exporting data");
            } catch (IOException ioException) {
                log.error("Error sending error response", ioException);
            }
        }
    }

    // ===== QR CODE OPERATIONS =====
    @PostMapping("/{id}/generate-qr")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<StudyListDTO> generateQRCode(@PathVariable Long id) {
        try {
            log.info("Generating QR code for study ID: {}", id);
            StudyListDTO updatedStudy = qrCodeService.generateQRCodeForStudy(id);
            return ResponseEntity.ok(updatedStudy);
        } catch (Exception e) {
            log.error("Error generating QR code for study ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/qr-image")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<byte[]> getQRCodeImage(@PathVariable Long id) {
        try {
            log.info("Getting QR code image for study ID: {}", id);
            byte[] qrImage = qrCodeService.getQRCodeImageForStudy(id);

            if (qrImage != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                return ResponseEntity.ok().headers(headers).body(qrImage);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting QR code image for study ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/qr/{qrCode}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<StudyListDTO> getStudyByQRCode(@PathVariable String qrCode) {
        try {
            log.info("Getting study by QR code: {}", qrCode);
            StudyListDTO study = qrCodeService.getStudyByQRCode(qrCode);

            if (study != null) {
                return ResponseEntity.ok(study);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting study by QR code: {}", qrCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== SEARCH/FILTER OPERATIONS =====
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<Page<StudyList>> searchStudies(
            @RequestParam(defaultValue = "") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Searching studies with filter: '{}', page: {}, size: {}", filter, page, size);

            org.springframework.data.domain.PageRequest pageRequest =
                    org.springframework.data.domain.PageRequest.of(page, size);

            Page<StudyList> studies = studyListService.listStudyListsByCode(filter, pageRequest);
            return ResponseEntity.ok(studies);

        } catch (Exception e) {
            log.error("Error searching studies", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== TEST ENDPOINT =====
    @GetMapping("/test")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "StudyList Database API is working");
        response.put("endpoint", "/api/studies");
        response.put("persistenceType", "Database (H2 with JPA/Hibernate)");
        response.put("timestamp", LocalDateTime.now());

        try {
            List<StudyListDTO> studies = studyListService.getAllStudyLists();
            response.put("studyCount", studies.size());
        } catch (Exception e) {
            response.put("studyCount", "Error: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCurrentUserPermissions() {
        Map<String, Object> permissions = new HashMap<>();

        String currentUser = getCurrentUsername();
        String userRole = getCurrentUserRole();
        boolean isAdmin = isCurrentUserAdmin();

        permissions.put("username", currentUser);
        permissions.put("role", userRole);
        permissions.put("isAdmin", isAdmin);
        permissions.put("permissions", Map.of(
                "read", true,
                "create", true,
                "update", true,
                "delete", isAdmin,
                "import", true,
                "export", true
        ));

        return ResponseEntity.ok(permissions);
    }

    // ===== HELPER METHODS =====
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains("\"")) {
            value = value.replace("\"", "\"\"");
        }
        return value;
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getUsername();
        }
        return "anonymous";
    }

    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getSimpleRoleName();
        }
        return "unknown";
    }

    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.isAdmin();
        }
        return false;
    }
}