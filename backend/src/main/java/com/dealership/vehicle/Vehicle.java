package com.dealership.vehicle;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a vehicle in the dealership inventory.
 * Stored in the "vehicles" table in PostgreSQL.
 *
 * Fields:
 *  - id        : UUID primary key
 *  - make      : manufacturer (e.g., "Toyota", "Ford")
 *  - model     : model name (e.g., "Camry", "Mustang")
 *  - category  : type (e.g., "Sedan", "SUV", "Truck")
 *  - price     : sale price in USD
 *  - quantity  : units currently in stock
 *  - createdAt : when the record was added
 *  - updatedAt : last update timestamp
 */
@Entity
@Table(name = "vehicles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Automatically update the updatedAt timestamp before every DB update.
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
