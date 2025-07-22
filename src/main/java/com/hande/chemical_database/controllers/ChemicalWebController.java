package com.hande.chemical_database.controllers;

import com.hande.chemical_database.entities.Chemicals;
import com.hande.chemical_database.models.ChemicalDTO;
import com.hande.chemical_database.repositories.ChemicalRepo;
import com.hande.chemical_database.services.ChemicalService;
import com.hande.chemical_database.services.ChemicalsFiltering;
import com.hande.chemical_database.services.ChemicalsUploadCsv;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
class ChemicalWebController {

    private final ChemicalService chemicalService;
    private final ChemicalRepo chemicalRepo;
    private final ChemicalsFiltering chemicalsFiltering;
    private final ChemicalsUploadCsv chemicalsUploadCsv;

    @GetMapping("/")
    public String home() {
        return "index"; // returns index.html or index.html from templates depending on setup
    }

    @GetMapping("/scanner")
    public String scanner() {
        return "scanner"; // you need to create scanner.html
    }

    @GetMapping("/chemicals-with-qr")
    public String chemicalsWithQr(Model model) {
        List<ChemicalDTO> chemicals = chemicalService.getAllChemicals(); // assuming service exists
        model.addAttribute("chemicals", chemicals);
        return "chemicals"; // you need to create chemicals.html
    }
}

