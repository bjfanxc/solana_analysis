package com.xps.solana.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.xps.solana")
public class SolonaApplication {
    private static final Logger logger = LoggerFactory.getLogger(SolonaApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Solona Analysis Application...");
        SpringApplication.run(SolonaApplication.class, args);
        logger.info("Solona Analysis Application started successfully!");
    }
}