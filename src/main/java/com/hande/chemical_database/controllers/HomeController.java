package com.hande.chemical_database.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class HomeController {

    @GetMapping("/")
    public String home() {
        log.info("Home page accessed");
        return "index";
    }

    @GetMapping("/home")
    public String homePage() {
        log.info("Home page accessed via /home");
        return "index";
    }

    @GetMapping("/scanner")
    public String qrScanner() {
        log.info("QR Scanner page accessed");
        return "qr-scanner";
    }

    @GetMapping("/test-db")
    public String testDatabase(Model model) {
        try {
            log.info("Testing system...");
            model.addAttribute("message", "System is working! Database test page.");
            return "db-test";
        } catch (Exception e) {
            log.error("Error in test", e);
            model.addAttribute("error", "Test error: " + e.getMessage());
            return "db-test";
        }
    }
}