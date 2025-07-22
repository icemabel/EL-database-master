package com.hande.chemical_database.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.hande.chemical_database.entities.Chemicals;
import com.hande.chemical_database.repositories.ChemicalRepo;
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
public class QRCodeService {

    private final ChemicalRepo chemicalRepo;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;

    public String generateQRCodeForChemical(Long chemicalId) {
        try {
            Chemicals chemical = chemicalRepo.findById(chemicalId)
                    .orElseThrow(() -> new RuntimeException("Chemical not found with id: " + chemicalId));

            // Generate unique QR code identifier
            String qrCodeId = UUID.randomUUID().toString();

            // Create QR code content (URL that will show chemical info)
            String qrContent = baseUrl + "/qr/" + qrCodeId;

            // Generate QR code image
            byte[] qrCodeImage = generateQRCodeImage(qrContent);

            // Update chemical with QR code info
            chemical.setQrCode(qrCodeId);
            chemical.setQrCodeImage(qrCodeImage);
            chemical.setQrCodeGeneratedAt(LocalDateTime.now());

            chemicalRepo.save(chemical);

            log.info("QR code generated for chemical: {} with ID: {}", chemical.getName(), qrCodeId);
            return qrCodeId;

        } catch (Exception e) {
            log.error("Error generating QR code for chemical ID: {}", chemicalId, e);
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

    public Chemicals getChemicalByQRCode(String qrCodeId) {
        return chemicalRepo.findByQrCode(qrCodeId)
                .orElseThrow(() -> new RuntimeException("Chemical not found for QR code: " + qrCodeId));
    }

    public byte[] getQRCodeImage(Long chemicalId) {
        Chemicals chemical = chemicalRepo.findById(chemicalId)
                .orElseThrow(() -> new RuntimeException("Chemical not found with id: " + chemicalId));

        if (chemical.getQrCodeImage() == null) {
            // Generate QR code if it doesn't exist
            generateQRCodeForChemical(chemicalId);
            chemical = chemicalRepo.findById(chemicalId).get();
        }

        return chemical.getQrCodeImage();
    }

    public void regenerateQRCode(Long chemicalId) {
        Chemicals chemical = chemicalRepo.findById(chemicalId)
                .orElseThrow(() -> new RuntimeException("Chemical not found with id: " + chemicalId));

        // Clear existing QR code data
        chemical.setQrCode(null);
        chemical.setQrCodeImage(null);
        chemical.setQrCodeGeneratedAt(null);

        // Generate new QR code
        generateQRCodeForChemical(chemicalId);
    }
}