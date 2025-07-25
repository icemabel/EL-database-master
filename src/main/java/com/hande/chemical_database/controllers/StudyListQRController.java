package com.hande.chemical_database.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class StudyListQRController {

    // Mock data for now - replace with actual service when database is set up
    private static final Map<String, Map<String, Object>> qrCodeDatabase = new HashMap<>();

    static {
        // Add some sample QR codes
        Map<String, Object> study1 = new HashMap<>();
        study1.put("studyCode", "CMQ_STD_001");
        study1.put("materialType", "tubing");
        study1.put("objectOfStudy", "Pharmline tube");
        study1.put("responsiblePerson", "Sabine");
        study1.put("status", "completed");
        study1.put("riskLevel", "Low");
        qrCodeDatabase.put("sample-qr-001", study1);
    }

    // Web endpoint for QR code scanning (shows study info)
    @GetMapping("/study-qr/{qrCodeId}")
    public String showStudyInfo(@PathVariable String qrCodeId, Model model) {
        try {
            log.info("QR scan request for study QR code ID: {}", qrCodeId);

            // Mock implementation - replace with actual service call
            Map<String, Object> study = qrCodeDatabase.get(qrCodeId);

            if (study != null) {
                model.addAttribute("study", study);
                return "study-info"; // Thymeleaf template
            } else {
                log.warn("Study not found for QR code ID: {}", qrCodeId);
                model.addAttribute("error", "Study not found for this QR code");
                return "error";
            }
        } catch (Exception e) {
            log.error("Error retrieving study for QR code ID: {}", qrCodeId, e);
            model.addAttribute("error", "Error retrieving study information");
            return "error";
        }
    }
}