package com.hande.chemical_database.services;

import com.hande.chemical_database.entities.Chemicals;
import com.hande.chemical_database.repositories.ChemicalRepo;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * 17/02/2025
 * handebarkan
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ChemicalsUploadCsvImpl implements ChemicalsUploadCsv {

    private final ChemicalRepo chemicalRepo;

    @Override
    public Integer uploadChemicals(MultipartFile file) {
        Set<Chemicals> chemicalsSet = null;
        try {
            chemicalsSet = parseCsv(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Check for existing endings and filter them out
        List<String> existingChemicals = chemicalRepo.findAllChemicalNames();
        chemicalsSet = chemicalsSet.stream()
                .filter(chemical -> !existingChemicals.contains(chemical.getName()))
                .collect(Collectors.toSet());

        chemicalRepo.saveAll(chemicalsSet);
        return chemicalsSet.size();
    }


    private Set<Chemicals> parseCsv(MultipartFile file) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            HeaderColumnNameMappingStrategy<Chemicals> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(Chemicals.class);

            CsvToBean<Chemicals> csvToBean = new CsvToBeanBuilder<Chemicals>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreEmptyLine(true)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            // Filter duplicates by using a HashSet to track seen endings
            Set<String> seenChemicals = new HashSet<>();
            return csvToBean.parse()
                    .stream()
                    .map(csvLine -> Chemicals.builder()
                            .name(csvLine.getName())
                            .CASNo(csvLine.getCASNo())
                            .LotNo(csvLine.getLotNo())
                            .producer(csvLine.getProducer())
                            .responsible(csvLine.getResponsible())
                            .storage(csvLine.getStorage())
                            .toxicState(csvLine.getToxicState())
                            .weight(csvLine.getWeight())
                            .orderDate(csvLine.getOrderDate())
                            .build())
                    .filter(chemical -> seenChemicals.add(chemical.getName())) // Add only unique endings
                    .collect(Collectors.toSet());
        }
    }
}
