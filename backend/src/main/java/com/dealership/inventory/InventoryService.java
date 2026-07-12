package com.dealership.inventory;

import com.dealership.vehicle.Vehicle;
import com.dealership.vehicle.VehicleRepository;
import com.dealership.vehicle.VehicleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling vehicle inventory operations: purchase and restock.
 *
 * Responsibilities:
 *  - purchase() : decrease vehicle quantity by 1 — throws if out of stock
 *  - restock()  : increase vehicle quantity by a given amount (Admin only)
 *
 * @Transactional ensures that quantity updates are atomic —
 * no partial saves if something fails mid-operation.
 *
 * Implemented AFTER InventoryServiceTest (TDD GREEN phase).
 */
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final VehicleRepository vehicleRepository;

    /**
     * Purchase a vehicle — decrements its quantity by 1.
     *
     * Business rule: A vehicle cannot be purchased if quantity is 0.
     * This is enforced at the service level, not just the UI.
     *
     * @param vehicleId UUID of the vehicle to purchase
     * @return updated VehicleResponse with new quantity
     * @throws RuntimeException if vehicle not found
     * @throws RuntimeException if vehicle is out of stock
     */
    @Transactional
    public VehicleResponse purchase(String vehicleId) {
        // Find vehicle or fail with a clear message
        Vehicle vehicle = findOrThrow(vehicleId);

        // Business rule: cannot purchase if out of stock
        if (vehicle.getQuantity() <= 0) {
            throw new RuntimeException("Vehicle is out of stock");
        }

        // Decrement quantity by exactly 1
        vehicle.setQuantity(vehicle.getQuantity() - 1);

        Vehicle saved = vehicleRepository.save(vehicle);
        return toResponse(saved);
    }

    /**
     * Restock a vehicle — increments its quantity by the given amount.
     * Admin-only operation (enforced at the controller level via role guard).
     *
     * @param vehicleId UUID of the vehicle to restock
     * @param request   RestockRequest containing the amount to add
     * @return updated VehicleResponse with new quantity
     * @throws RuntimeException if vehicle not found
     */
    @Transactional
    public VehicleResponse restock(String vehicleId, RestockRequest request) {
        Vehicle vehicle = findOrThrow(vehicleId);

        // Add the restock amount to current quantity
        vehicle.setQuantity(vehicle.getQuantity() + request.getAmount());

        Vehicle saved = vehicleRepository.save(vehicle);
        return toResponse(saved);
    }

    // ===== Private helpers =====

    /**
     * Find a vehicle by ID or throw a descriptive exception.
     */
    private Vehicle findOrThrow(String id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));
    }

    /**
     * Map Vehicle JPA entity to VehicleResponse DTO.
     */
    private VehicleResponse toResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .category(vehicle.getCategory())
                .price(vehicle.getPrice())
                .quantity(vehicle.getQuantity())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}
