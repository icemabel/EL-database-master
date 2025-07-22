package com.hande.chemical_database.services;

import com.hande.chemical_database.exceptions.ResourceNotFoundException;
import com.hande.chemical_database.entities.Chemicals;
import com.hande.chemical_database.mappers.ChemicalMapper;
import com.hande.chemical_database.models.ChemicalDTO;
import com.hande.chemical_database.repositories.ChemicalRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChemicalServiceImpl implements ChemicalService {

    private final ChemicalRepo chemicalRepo;
    private final ChemicalMapper chemicalMapper;
    private final QRCodeService qrCodeService;

    @Override
    public ChemicalDTO createChemicals(ChemicalDTO chemical) {
        Chemicals savedChemical = chemicalRepo.save(chemicalMapper.chemicalDTOToChemical(chemical));

        // Auto-generate QR code for new chemical
        try {
            qrCodeService.generateQRCodeForChemical(savedChemical.getId());
            // Reload the chemical to get the QR code info
            savedChemical = chemicalRepo.findById(savedChemical.getId()).orElse(savedChemical);
        } catch (Exception e) {
            log.warn("Failed to generate QR code for new chemical {}: {}", savedChemical.getName(), e.getMessage());
        }

        return chemicalMapper.chemicalToChemicalDTO(savedChemical);
    }

    @Override
    public Optional<ChemicalDTO> updateChemicals(ChemicalDTO chemical) {
        Optional<Chemicals> chemicalDb = chemicalRepo.findByNameIgnoreCase(chemical.getName());
        if(chemicalDb.isPresent()) {
            Chemicals chemicalUpdate = chemicalDb.get();
            chemicalUpdate.setName(chemical.getName());
            chemicalUpdate.setCASNo(chemical.getCASNo());
            chemicalUpdate.setLotNo(chemical.getLotNo());
            chemicalUpdate.setProducer(chemical.getProducer());
            chemicalUpdate.setStorage(chemical.getStorage());
            chemicalUpdate.setToxicState(chemical.getToxicState());
            chemicalUpdate.setResponsible(chemical.getResponsible());
            chemicalUpdate.setOrderDate(chemical.getOrderDate());
            chemicalUpdate.setWeight(chemical.getWeight());

            Chemicals updatedChemical = chemicalRepo.save(chemicalUpdate);
            return Optional.of(chemicalMapper.chemicalToChemicalDTO(updatedChemical));
        } else {
            throw new ResourceNotFoundException("Record not found with name : " + chemical.getName());
        }
    }

    @Override
    public List<ChemicalDTO> getAllChemicals() {
        return chemicalRepo.findAll().stream()
                .map(chemicalMapper::chemicalToChemicalDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ChemicalDTO> getChemicalById(Long id) {
        return Optional.ofNullable(chemicalMapper.chemicalToChemicalDTO(chemicalRepo.findById(id)
                .orElse(null)));
    }

    @Override
    public boolean deleteChemical(Long id) {
        Optional<Chemicals> chemicalsDb = chemicalRepo.findById(id);
        if(chemicalsDb.isPresent()) {
            chemicalRepo.deleteById(id);
            return true;
        } else {
            throw new ResourceNotFoundException("Record not found with id : " + id);
        }
    }

    @Override
    public boolean deleteChemicalByName(String name) {
        if(chemicalRepo.existsByNameIgnoreCase(name)) {
            chemicalRepo.deleteByName(name);
            return true;
        } else {
            throw new ResourceNotFoundException("Record not found with name : " + name);
        }
    }

    @Override
    public Page<Chemicals> listChemicalsByName(String filterCriteria, Pageable pageRequest) {
        return chemicalRepo.findByNameContainingIgnoreCase(filterCriteria, pageRequest);
    }
}