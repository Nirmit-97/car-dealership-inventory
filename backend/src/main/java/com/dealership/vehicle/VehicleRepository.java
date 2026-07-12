package com.dealership.vehicle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Vehicle entity.
 * Provides standard CRUD + a custom search query for filtering vehicles.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    /**
     * Search vehicles with optional filters.
     * All parameters are optional — null means "ignore this filter".
     *
     * Supports filtering by:
     *  - make        (case-insensitive partial match)
     *  - model       (case-insensitive partial match)
     *  - category    (case-insensitive partial match)
     *  - minPrice    (price >= minPrice)
     *  - maxPrice    (price <= maxPrice)
     *
     * Used by: GET /api/vehicles/search
     */
    @Query("SELECT v FROM Vehicle v WHERE " +
            "(:make IS NULL OR LOWER(v.make) LIKE LOWER(CONCAT('%', :make, '%'))) AND " +
            "(:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))) AND " +
            "(:category IS NULL OR LOWER(v.category) LIKE LOWER(CONCAT('%', :category, '%'))) AND " +
            "(:minPrice IS NULL OR v.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR v.price <= :maxPrice)")
    List<Vehicle> searchVehicles(
            @Param("make") String make,
            @Param("model") String model,
            @Param("category") String category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );
}
