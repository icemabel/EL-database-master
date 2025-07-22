package com.hande.chemical_database.models;

import com.opencsv.bean.CsvBindByName;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/*
 * 18/02/2025
 * handebarkan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChemicalsCsvRecord {
    @CsvBindByName
    private String name;
    @CsvBindByName
    private String CASNo;
    @CsvBindByName
    private String LotNo;
    @CsvBindByName
    private String producer;
    @CsvBindByName
    private String storage;
    @CsvBindByName
    private Boolean toxicState;
    @CsvBindByName
    private String responsible;
    @CsvBindByName
    private LocalDate orderDate;
    @CsvBindByName
    private String weight;
}
