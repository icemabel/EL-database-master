package com.hande.chemical_database.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class StudyListWebController {

    // Main study pages
    @GetMapping("/studies-with-qr")
    public String studiesWithQR() {
        log.info("Accessing studies list page with QR codes");
        return "study-qr-list-simple";
    }

    @GetMapping("/studies")
    public String studiesMain() {
        log.info("Accessing main studies page");
        return "study-qr-list-simple";
    }

    @GetMapping("/csv-import-export")
    public String csvImportExport() {
        log.info("Accessing studies CSV import/export page");
        return "csv-import-export-simple";
    }

    @GetMapping("/studies/csv")
    public String studiesCsv() {
        log.info("Accessing studies CSV import/export page (alternative route)");
        return "csv-import-export-simple";
    }
}