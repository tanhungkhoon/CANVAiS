package com.canvais;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the CANVAiS (Context-Aware Naturally Vibe AI Sketcher) application.
 * Bootstraps the Spring context, REST services, and embedded web map workspace.
 */
@SpringBootApplication
public class CanvaisApplication {

    /**
     * Main method executing the Spring Boot application bootstrap.
     *
     * @param args runtime command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CanvaisApplication.class, args);
    }
}
