package com.hande.chemical_database.controllers;

import com.hande.chemical_database.models.ChemicalDTO;
import com.hande.chemical_database.repositories.ChemicalRepo;
import com.hande.chemical_database.services.ChemicalService;
import com.hande.chemical_database.services.ChemicalsFiltering;
import com.hande.chemical_database.services.ChemicalsUploadCsv;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final ChemicalService chemicalService;
    private final ChemicalRepo chemicalRepo;
    private final ChemicalsFiltering chemicalsFiltering;
    private final ChemicalsUploadCsv chemicalsUploadCsv;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/home")
    public String homePage() {
        return "index";
    }

    @GetMapping("/csv-import-export")
    public String csvImportExport() {
        return "csv-import-export"; // returns csv-import-export.html
    }

    @GetMapping("/chemicals/manage")
    public String manageChemicals(Model model) {
        try {
            List<ChemicalDTO> chemicals = chemicalService.getAllChemicals();
            model.addAttribute("chemicals", chemicals);
            log.debug("Loading chemical management page with {} chemicals", chemicals.size());
        } catch (Exception e) {
            log.error("Error loading chemicals for management page", e);
            model.addAttribute("error", "Error loading chemicals: " + e.getMessage());
        }
        return "chemical-management"; // For future chemical management page
    }
}