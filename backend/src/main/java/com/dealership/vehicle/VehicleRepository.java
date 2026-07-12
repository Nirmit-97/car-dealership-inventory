package com.dealership.vehicle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for Vehicle entity.
 *
 * Extends JpaSpecificationExecutor<Vehicle> to support dynamic
 * Specification-based queries — replaces the broken JPQL @Query
 * that failed with null parameters on PostgreSQL (Hibernate 6 bytea bug).
 *
 * Search is now handled via VehicleSpecification.withFilters()
 * passed to findAll(Specification) — no custom SQL needed.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String>,
        JpaSpecificationExecutor<Vehicle> {
    // All CRUD methods from JpaRepository
    // All Specification-based findAll() overloads from JpaSpecificationExecutor
}
