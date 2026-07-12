package com.dealership.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for POST /api/vehicles/{id}/restock (Admin only).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestockRequest {

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Restock amount must be at least 1")
    private Integer amount;
}
