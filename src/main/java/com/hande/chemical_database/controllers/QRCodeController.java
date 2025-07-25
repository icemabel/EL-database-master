package com.hande.chemical_database.controllers;

import com.hande.chemical_database.entities.Chemicals;
import com.hande.chemical_database.services.QRCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class QRCodeController {

    private final QRCodeService qrCodeService;

    // Web endpoint for QR code scanning (shows chemical info)
    @GetMapping("/qr/{qrCodeId}")
    public String showChemicalInfo(@PathVariable String qrCodeId, Model model) {
        try {
            Chemicals chemical = qrCodeService.getChemicalByQRCode(qrCodeId);
            model.addAttribute("chemical", chemical);
            return "chemical-info"; // Thymeleaf template
        } catch (Exception e) {
            log.error("Error retrieving chemical for QR code ID: {}", qrCodeId, e);
            model.addAttribute("error", "Chemical not found for this QR code");
            return "error";
        }
    }

    // Web endpoint for QR code scanner page
//    @GetMapping("/scanner")
//    public String qrScanner() {
//        return "qr-scanner";
//    }

}