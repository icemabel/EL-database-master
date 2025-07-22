package com.hande.chemical_database.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Chemicals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(unique = true)
    private String name;

    private String CASNo;
    private String LotNo;
    private String producer;
    private String storage;
    private Integer quantity;

    @Column(nullable = true)
    private Boolean toxicState;

    private String responsible;
    private LocalDate orderDate;
    private String weight;

    // QR Code related fields
    @Column(unique = true)
    private String qrCode; // Unique identifier for QR code

    @Lob
    private byte[] qrCodeImage; // Store QR code image as bytes

    private LocalDateTime qrCodeGeneratedAt;

    // Constructors, getters, setters are handled by Lombok
}