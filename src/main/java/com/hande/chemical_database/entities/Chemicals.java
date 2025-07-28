package com.hande.chemical_database.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "chemicals")
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
    @Column(name = "name", unique = true, nullable = false, length = 255)
    private String name;

    @Column(name = "cas_no", length = 50)  // Database column: cas_no
    private String casNo;  // Java field: casNo (camelCase)

    @Column(name = "lot_no", length = 50)  // Database column: lot_no
    private String lotNo;  // Java field: lotNo (camelCase)

    @Column(name = "producer", length = 255)
    private String producer;

    @Column(name = "storage", nullable = false, length = 255)
    private String storage;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "toxic_state")  // Fixed: Use snake_case for database
    private Boolean toxicState;

    @Column(name = "responsible", length = 255)
    private String responsible;

    @Column(name = "order_date")  // Fixed: Use snake_case for database
    private LocalDate orderDate;

    @Column(name = "weight", length = 50)
    private String weight;

    // QR Code related fields
    @Column(name = "qr_code", unique = true, length = 36)
    private String qrCode; // Unique identifier for QR code

    @Lob
    @Column(name = "qr_code_image", columnDefinition = "LONGBLOB")
    private byte[] qrCodeImage; // Store QR code image as bytes

    @Column(name = "qr_code_generated_at")
    private LocalDateTime qrCodeGeneratedAt;

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    // Constructors, getters, setters are handled by Lombok
}