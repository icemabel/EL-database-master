package com.hande.chemical_database.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class ChemicalWebController {

    // Main chemical pages
    @GetMapping("/chemicals-with-qr")
    public String chemicalsWithQR() {
        log.info("Accessing chemicals list page");
        return "chemicals-with-qr"; // This should match the HTML filename
    }

    @GetMapping("/chemical-csv-import-export")
    public String chemicalCsvImportExport() {
        log.info("Accessing chemical CSV import/export page");
        return "chemical-qr-list-simple"; // Use your existing CSV page
    }

    // Alternative route for consistency
    @GetMapping("/chemicals/csv")
    public String chemicalsCsv() {
        log.info("Accessing chemical CSV import/export page (alternative route)");
        return "chemical-qr-list-simple"; // Use your existing CSV page
    }
}