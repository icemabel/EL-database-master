package com.hande.chemical_database.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
public class ChemicalDTO {
    private Long id;

    @NotBlank
    @NotNull
    private String name;

    private String casNo;  // Changed from CASNo to casNo (camelCase)
    private String lotNo;  // Changed from LotNo to lotNo (camelCase)
    private String producer;
    private Integer quantity;

    @NotBlank
    @NotNull
    private String storage;

    private Boolean toxicState;
    private String responsible;
    private LocalDate orderDate;
    private String weight;

    // QR Code fields
    private String qrCode;
    private LocalDateTime qrCodeGeneratedAt;

    // Note: qrCodeImage is not included in DTO to avoid large data transfer
    // Use separate endpoint to get QR code image
}