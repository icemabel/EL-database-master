package com.hande.chemical_database;

import com.hande.chemical_database.services.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class ChemicalDatabaseApplication implements CommandLineRunner {

	private final AdminService adminService;

	public static void main(String[] args) {
		SpringApplication.run(ChemicalDatabaseApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Create default admin user if it doesn't exist
		try {
			adminService.createDefaultAdminIfNotExists();
			log.info("Application started successfully");
			log.info("Default admin credentials: username=admin, password=password");
			log.info("Please change the default password after first login");
		} catch (Exception e) {
			log.error("Error during application startup", e);
		}
	}
}