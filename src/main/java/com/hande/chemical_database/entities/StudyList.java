package com.hande.chemical_database.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_list")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class StudyList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "study_code", unique = true, nullable = false, length = 100)
    private String studyCode;

    @Column(name = "document_codes", length = 500)
    private String documentCodes;

    @Column(name = "material_type", length = 100)
    private String materialType;

    @Column(name = "study_level", length = 10)
    private String studyLevel;

    @Column(name = "risk_level", length = 50)
    private String riskLevel;

    @Column(name = "info", length = 1000)
    private String info;

    @Column(name = "number_of_samples", length = 200)
    private String numberOfSamples;

    @Column(name = "object_of_study", length = 500)
    private String objectOfStudy;

    @Column(name = "responsible_person", length = 100)
    private String responsiblePerson;

    @Column(name = "status", length = 50)
    private String status;

    // QR Code related fields
    @Column(name = "qr_code", unique = true, length = 36)
    private String qrCode;

    @Lob
    @Column(name = "qr_code_image", columnDefinition = "LONGBLOB")
    private byte[] qrCodeImage;

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
}