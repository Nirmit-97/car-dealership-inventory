package com.dealership.inventory;

import com.dealership.vehicle.Vehicle;

/**
 * Utility class encapsulating stock-level business rules.
 *
 * REFACTOR reason:
 *   The out-of-stock check was previously inline inside InventoryService.purchase().
 *   Extracting it here makes the rule:
 *     1. Testable in isolation
 *     2. Reusable if other services need it (e.g., a bulk-purchase feature)
 *     3. Named — "StockValidator" makes the intent immediately clear
 *
 * Static methods — no state, no Spring bean needed.
 */
public final class StockValidator {

    private StockValidator() {} // prevent instantiation

    /**
     * Validates that a vehicle has sufficient stock for purchase.
     *
     * @param vehicle the vehicle to check
     * @throws RuntimeException if quantity is 0 or less
     */
    public static void validateInStock(Vehicle vehicle) {
        if (vehicle.getQuantity() <= 0) {
            throw new RuntimeException(
                    "Vehicle is out of stock: " + vehicle.getMake() + " " + vehicle.getModel()
            );
        }
    }
}
