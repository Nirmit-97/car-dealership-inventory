package com.dealership.inventory;

import com.dealership.vehicle.VehicleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for inventory operations: purchase and restock.
 *
 * Endpoints:
 *  POST /api/vehicles/{id}/purchase  — purchase a vehicle (any authenticated user)
 *  POST /api/vehicles/{id}/restock   — restock a vehicle (Admin only)
 *
 * Nested under /api/vehicles/{id} to make the resource relationship clear.
 */
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Purchase a vehicle — decrements its quantity by 1.
     * POST /api/vehicles/{id}/purchase
     *
     * Available to: all authenticated users (USER and ADMIN)
     *
     * @param id the vehicle UUID to purchase
     * @return 200 with updated vehicle (new quantity = old - 1)
     * @return 400 if vehicle is out of stock
     * @return 404 if vehicle not found
     */
    @PostMapping("/{id}/purchase")
    public ResponseEntity<VehicleResponse> purchase(@PathVariable String id) {
        VehicleResponse response = inventoryService.purchase(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Restock a vehicle — increments its quantity by the given amount.
     * POST /api/vehicles/{id}/restock
     *
     * Admin only — enforced by SecurityConfig and @PreAuthorize.
     *
     * Request body: { "amount": 10 }
     *
     * @param id      the vehicle UUID to restock
     * @param request RestockRequest with the amount to add
     * @return 200 with updated vehicle (new quantity = old + amount)
     * @return 400 if amount < 1
     * @return 404 if vehicle not found
     */
    @PostMapping("/{id}/restock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponse> restock(
            @PathVariable String id,
            @Valid @RequestBody RestockRequest request) {
        VehicleResponse response = inventoryService.restock(id, request);
        return ResponseEntity.ok(response);
    }
}
