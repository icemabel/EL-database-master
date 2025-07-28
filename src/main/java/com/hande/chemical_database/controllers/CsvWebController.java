package com.hande.chemical_database.controllers;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//
//@Controller
//@Slf4j
//public class CsvWebController {
//
//    // ===== MAIN CSV PAGES =====
//
//    /**
//     * Main unified CSV page with 2x2 grid layout for both chemicals and studies
//     * This is the recommended entry point for CSV operations
//     */
//    @GetMapping("/csv-unified")
//    public String csvUnified() {
//        log.info("Accessing unified CSV import/export page (2x2 grid)");
//        return "csv-import-export-unified";
//    }
//
//    /**
//     * CSV Debug page for troubleshooting CSV parsing issues
//     */
//    @GetMapping("/csv-debug")
//    public String csvDebug() {
//        log.info("Accessing CSV debug page");
//        return "csv-debug";
//    }
//
//    // ===== ENTITY-SPECIFIC PAGES =====
//
//    /**
//     * Studies list page with QR code functionality
//     */
//    @GetMapping("/studies-with-qr")
//    public String studiesWithQR() {
//        log.info("Accessing studies list page");
//        return "study-qr-list-simple";
//    }
//
//    /**
//     * Studies-only CSV import/export page
//     */
//    @GetMapping("/csv-import-export")
//    public String csvImportExport() {
//        log.info("Accessing studies CSV import/export page (studies only)");
//        return "csv-import-export-simple";
//    }
//
//    /**
//     * Chemicals list page with QR code functionality
//     */
//    @GetMapping("/chemicals-with-qr")
//    public String chemicalsWithQR() {
//        log.info("Accessing chemicals list page");
//        return "chemical-qr-list-simple";
//    }
//
//    /**
//     * Chemicals-only CSV import/export page
//     */
//    @GetMapping("/chemicals-csv")
//    public String chemicalsCsv() {
//        log.info("Accessing chemicals-specific CSV page");
//        return "csv-import-export"; // The original chemicals CSV page
//    }
//
//    // ===== ALTERNATIVE ROUTES FOR CONVENIENCE =====
//
//    /**
//     * Main CSV route - redirects to unified page
//     */
//    @GetMapping("/csv")
//    public String csvMain() {
//        log.info("Accessing main CSV page (redirecting to unified)");
//        return "csv-import-export-unified";
//    }
//
//    /**
//     * Import/Export route - redirects to unified page
//     */
//    @GetMapping("/import-export")
//    public String importExport() {
//        log.info("Accessing import/export page (redirecting to unified)");
//        return "csv-import-export-unified";
//    }
//
//    /**
//     * Studies-specific CSV route
//     */
//    @GetMapping("/studies-csv")
//    public String studiesCsv() {
//        log.info("Accessing studies-specific CSV page");
//        return "csv-import-export-simple";
//    }
//
//    // ===== DEBUG AND TESTING ROUTES =====
//
//    /**
//     * CSV test route - redirects to debug page
//     */
//    @GetMapping("/csv-test")
//    public String csvTest() {
//        log.info("Accessing CSV test page (redirecting to debug)");
//        return "csv-debug";
//    }
//}

