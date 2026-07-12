package com.dealership;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Car Dealership Inventory System.
 * Spring Boot auto-configures JPA, Security, and Web layers.
 */
@SpringBootApplication
public class DealershipApplication {

    public static void main(String[] args) {
        SpringApplication.run(DealershipApplication.class, args);
    }
}
