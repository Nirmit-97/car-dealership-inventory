package com.dealership.vehicle;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for vehicle CRUD and search operations.
 *
 * All endpoints require a valid JWT (configured in SecurityConfig).
 * DELETE is restricted to ADMIN role only.
 *
 * Endpoints:
 *  POST   /api/vehicles              — add a new vehicle
 *  GET    /api/vehicles              — list all vehicles
 *  GET    /api/vehicles/search       — search with optional filters
 *  GET    /api/vehicles/{id}         — get single vehicle
 *  PUT    /api/vehicles/{id}         — update a vehicle
 *  DELETE /api/vehicles/{id}         — delete a vehicle (Admin only)
 */
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    /**
     * Add a new vehicle to the inventory.
     * POST /api/vehicles
     *
     * @param request vehicle data (make, model, category, price, quantity)
     * @return 201 Created with the saved vehicle
     */
    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all vehicles in the inventory.
     * GET /api/vehicles
     *
     * @return 200 with list of all vehicles
     */
    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    /**
     * Search vehicles with optional filters.
     * GET /api/vehicles/search?make=Toyota&category=Sedan&minPrice=10000&maxPrice=50000
     *
     * All parameters are optional — omit any to not filter by that field.
     *
     * @return 200 with filtered list (may be empty)
     */
    @GetMapping("/search")
    public ResponseEntity<List<VehicleResponse>> searchVehicles(
            @RequestParam(required = false) String make,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        return ResponseEntity.ok(
                vehicleService.searchVehicles(make, model, category, minPrice, maxPrice)
        );
    }

    /**
     * Get a single vehicle by ID.
     * GET /api/vehicles/{id}
     *
     * @return 200 with vehicle, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable String id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    /**
     * Update an existing vehicle's details.
     * PUT /api/vehicles/{id}
     *
     * @return 200 with updated vehicle, or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @PathVariable String id,
            @Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.ok(vehicleService.updateVehicle(id, request));
    }

    /**
     * Delete a vehicle from the inventory.
     * DELETE /api/vehicles/{id}
     *
     * Admin only — enforced by both SecurityConfig and @PreAuthorize.
     *
     * @return 204 No Content on success, or 404 if not found
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVehicle(@PathVariable String id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
