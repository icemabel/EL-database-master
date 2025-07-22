package com.hande.chemical_database.controllers;

import com.hande.chemical_database.entities.Chemicals;
import com.hande.chemical_database.models.ChemicalDTO;
import com.hande.chemical_database.services.ChemicalService;
import com.hande.chemical_database.services.ChemicalsFiltering;
import com.hande.chemical_database.services.ChemicalsUploadCsv;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/chemicals")
@RequiredArgsConstructor
@Slf4j
public class ChemicalController {

    private final ChemicalService chemicalService;
    private final ChemicalsFiltering chemicalsFiltering;
    private final ChemicalsUploadCsv chemicalsUploadCsv;

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<ChemicalDTO>> getAllChemicals() {
        List<ChemicalDTO> chemicals = chemicalService.getAllChemicals();
        return ResponseEntity.ok(chemicals);
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ChemicalDTO> getChemicalById(@PathVariable Long id) {
        Optional<ChemicalDTO> chemical = chemicalService.getChemicalById(id);
        return chemical.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<ChemicalDTO> searchByName(@RequestParam String name) {
        Optional<ChemicalDTO> chemical = chemicalsFiltering.searchByName(name);
        return chemical.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search-cas")
    @ResponseBody
    public ResponseEntity<List<Chemicals>> searchByCAS(@RequestParam String casNo) {
        List<Chemicals> chemicals = chemicalsFiltering.searchByCASNo(casNo);
        return ResponseEntity.ok(chemicals);
    }

    @GetMapping("/filter")
    @ResponseBody
    public ResponseEntity<Page<Chemicals>> filterChemicals(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String storage,
            @RequestParam(required = false) String responsible,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "25") Integer size) {

        PageRequest pageRequest = chemicalsFiltering.buildPageRequest(page, size);

        // Create a dummy chemical object for filtering
        Chemicals filterCriteria = Chemicals.builder()
                .name(name)
                .storage(storage)
                .responsible(responsible)
                .build();

        Page<Chemicals> result;
        if (name != null && !name.trim().isEmpty()) {
            result = chemicalsFiltering.listChemicalsByName(filterCriteria, pageRequest);
        } else if (storage != null && !storage.trim().isEmpty()) {
            result = chemicalsFiltering.listChemicalsByStorage(filterCriteria, pageRequest);
        } else if (responsible != null && !responsible.trim().isEmpty()) {
            result = chemicalsFiltering.listChemicalsByOwner(filterCriteria, pageRequest);
        } else {
            result = chemicalsFiltering.listChemicalsByName(filterCriteria, pageRequest);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/toxic")
    @ResponseBody
    public ResponseEntity<Page<Chemicals>> getToxicChemicals(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "25") Integer size) {

        PageRequest pageRequest = chemicalsFiltering.buildPageRequest(page, size);
        Chemicals filterCriteria = Chemicals.builder().build();
        Page<Chemicals> toxicChemicals = chemicalsFiltering.showToxicChemicals(filterCriteria, pageRequest);

        return ResponseEntity.ok(toxicChemicals);
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<ChemicalDTO> createChemical(@Valid @RequestBody ChemicalDTO chemicalDTO) {
        try {
            ChemicalDTO createdChemical = chemicalService.createChemicals(chemicalDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdChemical);
        } catch (Exception e) {
            log.error("Error creating chemical: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping
    @ResponseBody
    public ResponseEntity<ChemicalDTO> updateChemical(@Valid @RequestBody ChemicalDTO chemicalDTO) {
        try {
            Optional<ChemicalDTO> updatedChemical = chemicalService.updateChemicals(chemicalDTO);
            return updatedChemical.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error updating chemical: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteChemical(@PathVariable Long id) {
        try {
            boolean deleted = chemicalService.deleteChemical(id);
            return deleted ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting chemical: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/name/{name}")
    @ResponseBody
    public ResponseEntity<Void> deleteChemicalByName(@PathVariable String name) {
        try {
            boolean deleted = chemicalService.deleteChemicalByName(name);
            return deleted ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting chemical by name: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/upload-csv")
    @ResponseBody
    public ResponseEntity<String> uploadChemicalsFromCsv(@RequestParam("file") MultipartFile file) {
        try {
            Integer uploadedCount = chemicalsUploadCsv.uploadChemicals(file);
            String message = String.format("Successfully uploaded %d chemicals", uploadedCount);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error uploading CSV: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing CSV file");
        }
    }
}
