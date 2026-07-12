package com.dealership.vehicle;

/**
 * Utility class for mapping Vehicle JPA entities to VehicleResponse DTOs.
 *
 * REFACTOR reason:
 *   Both VehicleService and InventoryService previously had identical private
 *   toResponse() methods — a clear DRY violation. Extracting to a shared mapper
 *   ensures a single source of truth for the mapping logic.
 *
 * Static methods used intentionally — no state, no Spring bean needed.
 * Prevents instantiation via private constructor.
 */
public final class VehicleMapper {

    // Prevent instantiation — this is a pure utility class
    private VehicleMapper() {}

    /**
     * Maps a Vehicle entity to a VehicleResponse DTO.
     * Used by VehicleService and InventoryService.
     *
     * @param vehicle the JPA entity
     * @return VehicleResponse DTO safe to expose in the API
     */
    public static VehicleResponse toResponse(Vehicle vehicle) {
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
