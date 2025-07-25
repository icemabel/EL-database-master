package com.hande.chemical_database.services;

import org.springframework.web.multipart.MultipartFile;

public interface StudyListUploadCsv {
    Integer uploadStudyLists(MultipartFile file);
}