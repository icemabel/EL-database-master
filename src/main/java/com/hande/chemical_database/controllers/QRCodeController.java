package com.hande.chemical_database.controllers;

import com.hande.chemical_database.entities.Chemicals;
import com.hande.chemical_database.services.QRCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class QRCodeController {

    private final QRCodeService qrCodeService;

    // API endpoint to generate QR code for a chemical
    @PostMapping("/api/chemicals/{id}/generate-qr")
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

    // API endpoint to get QR code image
    @GetMapping("/api/chemicals/{id}/qr-image")
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

    // API endpoint to regenerate QR code
    @PostMapping("/api/chemicals/{id}/regenerate-qr")
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

    // Web endpoint for QR code scanning (shows chemical info)
    @GetMapping("/qr/{qrCodeId}")
    public String showChemicalInfo(@PathVariable String qrCodeId, Model model) {
        try {
            Chemicals chemical = qrCodeService.getChemicalByQRCode(qrCodeId);
            model.addAttribute("chemical", chemical);
            return "chemical-info"; // Thymeleaf template
        } catch (Exception e) {
            log.error("Error retrieving chemical for QR code ID: {}", qrCodeId, e);
            model.addAttribute("error", "Chemical not found for this QR code");
            return "error";
        }
    }

    // Web endpoint for QR code scanner page
    @GetMapping("/scanner")
    public String qrScanner() {
        return "qr-scanner";
    }

    // Web endpoint for chemical list with QR codes
    @GetMapping("/chemicals-with-qr")
    public String chemicalsWithQR(Model model) {
        // This will be handled by a separate controller method
        return "chemicals-qr-list";
    }
}