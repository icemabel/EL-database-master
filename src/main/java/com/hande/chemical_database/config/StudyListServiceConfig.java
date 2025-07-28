package com.hande.chemical_database.config;

import com.hande.chemical_database.services.StudyListQRCodeService;
import com.hande.chemical_database.services.StudyListServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

//import javax.annotation.PostConstruct;
import jakarta.annotation.PostConstruct;


/**
 * Configuration to handle circular dependency between StudyListService and StudyListQRCodeService
 */
@Configuration
@RequiredArgsConstructor
public class StudyListServiceConfig {

    private final StudyListServiceImpl studyListService;
    private final StudyListQRCodeService qrCodeService;

    @PostConstruct
    public void configureServices() {
        // Set up the QR code service reference to avoid circular dependency
        studyListService.setQrCodeService(qrCodeService);
    }
}