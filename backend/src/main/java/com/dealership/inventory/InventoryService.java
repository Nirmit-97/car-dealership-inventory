package com.dealership.inventory;

import com.dealership.vehicle.Vehicle;
import com.dealership.vehicle.VehicleMapper;
import com.dealership.vehicle.VehicleRepository;
import com.dealership.vehicle.VehicleResponse;
import com.dealership.vehicle.VehicleService;
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
 * REFACTOR: findOrThrow() was duplicated from VehicleService.
 * Now delegates to VehicleService.findVehicleEntity() — single source of truth
 * for the "vehicle not found" lookup and error message.
 */
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final VehicleRepository vehicleRepository;
    private final VehicleService vehicleService;   // ← replaces duplicate findOrThrow()

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
        // Delegate lookup to VehicleService — no duplicate findOrThrow
        Vehicle vehicle = vehicleService.findVehicleEntity(vehicleId);

        // Business rule: cannot purchase if out of stock
        if (vehicle.getQuantity() <= 0) {
            throw new RuntimeException("Vehicle is out of stock");
        }

        // Decrement quantity by exactly 1
        vehicle.setQuantity(vehicle.getQuantity() - 1);

        Vehicle saved = vehicleRepository.save(vehicle);
        return VehicleMapper.toResponse(saved);
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
        // Delegate lookup to VehicleService — no duplicate findOrThrow
        Vehicle vehicle = vehicleService.findVehicleEntity(vehicleId);

        // Add the restock amount to current quantity
        vehicle.setQuantity(vehicle.getQuantity() + request.getAmount());

        Vehicle saved = vehicleRepository.save(vehicle);
        return VehicleMapper.toResponse(saved);
    }

    // toResponse()    extracted to VehicleMapper   — see VehicleMapper.java
    // findOrThrow()   extracted to VehicleService  — see VehicleService.findVehicleEntity()
}
