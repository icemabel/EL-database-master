package com.hande.chemical_database.services;

import org.springframework.web.multipart.MultipartFile;

/*
 * 17/02/2025
 * handebarkan
 */
public interface ChemicalsUploadCsv {
    Integer uploadChemicals(MultipartFile file);
}
