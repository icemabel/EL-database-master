package com.hande.chemical_database.services;

import org.springframework.web.multipart.MultipartFile;

public interface StudyListUploadCsv {
    String uploadStudyListsFromCsv(MultipartFile file);  // Changed return type to String for message
    Integer uploadStudyLists(MultipartFile file);        // Keep the old method for backward compatibility
}