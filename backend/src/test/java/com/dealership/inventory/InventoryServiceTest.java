package com.dealership.inventory;

import com.dealership.vehicle.Vehicle;
import com.dealership.vehicle.VehicleRepository;
import com.dealership.vehicle.VehicleResponse;
import com.dealership.vehicle.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TDD Unit Tests for InventoryService.
 *
 * Updated in REFACTOR phase:
 *   InventoryService now injects VehicleService (not raw VehicleRepository)
 *   for findVehicleEntity() — this test reflects that change.
 *   VehicleRepository is still mocked for the save() operation.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleService vehicleService;      // ← replaces direct repository lookup

    @InjectMocks
    private InventoryService inventoryService;

    // Test fixtures
    private Vehicle inStockVehicle;
    private Vehicle outOfStockVehicle;

    @BeforeEach
    void setUp() {
        inStockVehicle = Vehicle.builder()
                .id("v-001")
                .make("Toyota")
                .model("Camry")
                .category("Sedan")
                .price(25000.00)
                .quantity(5)
                .build();

        outOfStockVehicle = Vehicle.builder()
                .id("v-002")
                .make("Ford")
                .model("F-150")
                .category("Truck")
                .price(40000.00)
                .quantity(0)
                .build();
    }

    // =========================================================
    // PURCHASE TESTS
    // =========================================================

    @Test
    @DisplayName("purchase: should decrement quantity by 1 when vehicle is in stock")
    void purchase_shouldDecrementQuantity_whenInStock() {
        // ARRANGE
        when(vehicleService.findVehicleEntity("v-001")).thenReturn(inStockVehicle);

        Vehicle afterPurchase = Vehicle.builder()
                .id("v-001").make("Toyota").model("Camry")
                .category("Sedan").price(25000.00).quantity(4).build();
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(afterPurchase);

        // ACT
        VehicleResponse response = inventoryService.purchase("v-001");

        // ASSERT
        assertThat(response.getQuantity()).isEqualTo(4);
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("purchase: should throw RuntimeException when vehicle is out of stock")
    void purchase_shouldThrow_whenQuantityIsZero() {
        // ARRANGE
        when(vehicleService.findVehicleEntity("v-002")).thenReturn(outOfStockVehicle);

        // ACT + ASSERT
        assertThatThrownBy(() -> inventoryService.purchase("v-002"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("out of stock");

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    @DisplayName("purchase: should throw RuntimeException when vehicle ID not found")
    void purchase_shouldThrow_whenVehicleNotFound() {
        // ARRANGE — VehicleService throws on bad ID
        when(vehicleService.findVehicleEntity("bad-id"))
                .thenThrow(new RuntimeException("Vehicle not found with id: bad-id"));

        // ACT + ASSERT
        assertThatThrownBy(() -> inventoryService.purchase("bad-id"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Vehicle not found");

        verify(vehicleRepository, never()).save(any());
    }

    // =========================================================
    // RESTOCK TESTS
    // =========================================================

    @Test
    @DisplayName("restock: should increment quantity by the given amount")
    void restock_shouldIncrementQuantityByAmount() {
        // ARRANGE
        when(vehicleService.findVehicleEntity("v-001")).thenReturn(inStockVehicle);

        Vehicle afterRestock = Vehicle.builder()
                .id("v-001").make("Toyota").model("Camry")
                .category("Sedan").price(25000.00).quantity(15).build();
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(afterRestock);

        // ACT
        RestockRequest request = new RestockRequest(10);
        VehicleResponse response = inventoryService.restock("v-001", request);

        // ASSERT
        assertThat(response.getQuantity()).isEqualTo(15);
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("restock: should restore a previously out-of-stock vehicle")
    void restock_shouldWork_forOutOfStockVehicle() {
        // ARRANGE
        when(vehicleService.findVehicleEntity("v-002")).thenReturn(outOfStockVehicle);

        Vehicle afterRestock = Vehicle.builder()
                .id("v-002").make("Ford").model("F-150")
                .category("Truck").price(40000.00).quantity(20).build();
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(afterRestock);

        // ACT
        RestockRequest request = new RestockRequest(20);
        VehicleResponse response = inventoryService.restock("v-002", request);

        // ASSERT
        assertThat(response.getQuantity()).isEqualTo(20);
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("restock: should throw RuntimeException when vehicle ID not found")
    void restock_shouldThrow_whenVehicleNotFound() {
        // ARRANGE — VehicleService throws on bad ID
        when(vehicleService.findVehicleEntity("bad-id"))
                .thenThrow(new RuntimeException("Vehicle not found with id: bad-id"));

        // ACT + ASSERT
        RestockRequest request = new RestockRequest(5);
        assertThatThrownBy(() -> inventoryService.restock("bad-id", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Vehicle not found");

        verify(vehicleRepository, never()).save(any());
    }
}
