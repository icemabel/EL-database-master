package com.hande.chemical_database.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class StudyListDTO {
    private Long id;

    @NotBlank
    @NotNull
    private String studyCode;

    private String documentCodes;
    private String materialType;
    private String studyLevel;
    private String riskLevel;
    private String info;
    private String numberOfSamples;
    private String objectOfStudy;
    private String responsiblePerson;
    private String status;

    // QR Code fields
    private String qrCode;
    private LocalDateTime qrCodeGeneratedAt;

    // Note: qrCodeImage is not included in DTO to avoid large data transfer
    // Use separate endpoint to get QR code image
}