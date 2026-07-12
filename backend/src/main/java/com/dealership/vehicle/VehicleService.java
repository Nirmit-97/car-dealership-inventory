package com.dealership.vehicle;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service handling all vehicle CRUD and search operations.
 *
 * Responsibilities:
 *  - createVehicle()   : persist a new vehicle, return response DTO
 *  - getAllVehicles()  : return all vehicles as response DTOs
 *  - getVehicleById() : return single vehicle or throw if not found
 *  - searchVehicles() : filter by make, model, category, price range
 *  - updateVehicle()  : update fields of existing vehicle
 *  - deleteVehicle()  : remove vehicle by ID
 *
 * Implemented AFTER VehicleServiceTest (TDD GREEN phase).
 */
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    /**
     * Create and persist a new vehicle.
     *
     * @param request VehicleRequest with make, model, category, price, quantity
     * @return VehicleResponse with generated ID and all fields
     */
    public VehicleResponse createVehicle(VehicleRequest request) {
        Vehicle vehicle = Vehicle.builder()
                .make(request.getMake())
                .model(request.getModel())
                .category(request.getCategory())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        return VehicleMapper.toResponse(saved);
    }

    /**
     * Retrieve all vehicles in the inventory.
     *
     * @return list of VehicleResponse (empty list if none exist)
     */
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll()
                .stream()
                .map(VehicleMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a single vehicle by its ID.
     *
     * @param id UUID of the vehicle
     * @return VehicleResponse
     * @throws RuntimeException if vehicle not found
     */
    public VehicleResponse getVehicleById(String id) {
        Vehicle vehicle = findVehicleEntity(id);
        return VehicleMapper.toResponse(vehicle);
    }

    /**
     * Search vehicles with optional filters.
     * Any null parameter is ignored (treated as "no filter").
     *
     * @param make      optional manufacturer filter
     * @param model     optional model name filter
     * @param category  optional category filter
     * @param minPrice  optional minimum price filter
     * @param maxPrice  optional maximum price filter
     * @return filtered list of VehicleResponse
     */
    public List<VehicleResponse> searchVehicles(
            String make, String model, String category,
            Double minPrice, Double maxPrice) {

        return vehicleRepository
                .findAll(VehicleSpecification.withFilters(make, model, category, minPrice, maxPrice))
                .stream()
                .map(VehicleMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing vehicle's details.
     *
     * @param id      UUID of the vehicle to update
     * @param request new values for make, model, category, price, quantity
     * @return updated VehicleResponse
     * @throws RuntimeException if vehicle not found
     */
    public VehicleResponse updateVehicle(String id, VehicleRequest request) {
        Vehicle vehicle = findVehicleEntity(id);

        // Apply updates — only modify what's provided
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setCategory(request.getCategory());
        vehicle.setPrice(request.getPrice());
        vehicle.setQuantity(request.getQuantity());

        Vehicle updated = vehicleRepository.save(vehicle);
        return VehicleMapper.toResponse(updated);
    }

    /**
     * Delete a vehicle by ID.
     *
     * @param id UUID of the vehicle to delete
     * @throws RuntimeException if vehicle not found
     */
    public void deleteVehicle(String id) {
        Vehicle vehicle = findVehicleEntity(id);
        vehicleRepository.delete(vehicle);
    }

    // ===== Private helpers =====

    /**
     * Find a vehicle entity by ID or throw a descriptive RuntimeException.
     *
     * Public so InventoryService can reuse this lookup without duplicating it.
     * This eliminates the identical findOrThrow() that previously lived in both services.
     *
     * REFACTOR: centralize vehicle lookup — single place to change the error message
     * or swap the repository call in the future.
     */
    public Vehicle findVehicleEntity(String id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));
    }

    // toResponse() extracted to VehicleMapper — see VehicleMapper.java
}
