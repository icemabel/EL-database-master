package com.hande.chemical_database.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.hande.chemical_database.entities.StudyList;
import com.hande.chemical_database.repositories.StudyListRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudyListQRCodeService {

    private final StudyListRepo studyListRepo;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;

    public String generateQRCodeForStudyList(Long studyListId) {
        try {
            StudyList studyList = studyListRepo.findById(studyListId)
                    .orElseThrow(() -> new RuntimeException("StudyList not found with id: " + studyListId));

            // Generate unique QR code identifier
            String qrCodeId = UUID.randomUUID().toString();

            // Create QR code content (URL that will show study info)
            String qrContent = baseUrl + "/study-qr/" + qrCodeId;

            // Generate QR code image
            byte[] qrCodeImage = generateQRCodeImage(qrContent);

            // Update study with QR code info
            studyList.setQrCode(qrCodeId);
            studyList.setQrCodeImage(qrCodeImage);
            studyList.setQrCodeGeneratedAt(LocalDateTime.now());

            studyListRepo.save(studyList);

            log.info("QR code generated for study: {} with ID: {}", studyList.getStudyCode(), qrCodeId);
            return qrCodeId;

        } catch (Exception e) {
            log.error("Error generating QR code for study ID: {}", studyListId, e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    public byte[] generateQRCodeImage(String content) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return outputStream.toByteArray();
    }

    public StudyList getStudyListByQRCode(String qrCodeId) {
        return studyListRepo.findByQrCode(qrCodeId)
                .orElseThrow(() -> new RuntimeException("StudyList not found for QR code: " + qrCodeId));
    }

    public byte[] getQRCodeImage(Long studyListId) {
        StudyList studyList = studyListRepo.findById(studyListId)
                .orElseThrow(() -> new RuntimeException("StudyList not found with id: " + studyListId));

        if (studyList.getQrCodeImage() == null) {
            // Generate QR code if it doesn't exist
            generateQRCodeForStudyList(studyListId);
            studyList = studyListRepo.findById(studyListId).get();
        }

        return studyList.getQrCodeImage();
    }

    public void regenerateQRCode(Long studyListId) {
        StudyList studyList = studyListRepo.findById(studyListId)
                .orElseThrow(() -> new RuntimeException("StudyList not found with id: " + studyListId));

        // Clear existing QR code data
        studyList.setQrCode(null);
        studyList.setQrCodeImage(null);
        studyList.setQrCodeGeneratedAt(null);

        // Generate new QR code
        generateQRCodeForStudyList(studyListId);
    }
}