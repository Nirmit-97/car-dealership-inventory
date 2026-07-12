package com.dealership.inventory;

import com.dealership.vehicle.Vehicle;
import com.dealership.vehicle.VehicleRepository;
import com.dealership.vehicle.VehicleResponse;
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
 * RED phase: Written BEFORE InventoryService exists.
 * Expected to FAIL until InventoryService is implemented (GREEN phase).
 *
 * Covers:
 *  - purchase(): decrement quantity, throw when out of stock, throw when not found
 *  - restock() : increment quantity, throw when not found, throw on bad amount
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

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
                .quantity(5)        // has stock
                .build();

        outOfStockVehicle = Vehicle.builder()
                .id("v-002")
                .make("Ford")
                .model("F-150")
                .category("Truck")
                .price(40000.00)
                .quantity(0)        // out of stock
                .build();
    }

    // =========================================================
    // PURCHASE TESTS
    // =========================================================

    @Test
    @DisplayName("purchase: should decrement quantity by 1 when vehicle is in stock")
    void purchase_shouldDecrementQuantity_whenInStock() {
        // ARRANGE
        when(vehicleRepository.findById("v-001")).thenReturn(Optional.of(inStockVehicle));

        // After save, quantity should be 4 (5 - 1)
        Vehicle afterPurchase = Vehicle.builder()
                .id("v-001")
                .make("Toyota")
                .model("Camry")
                .category("Sedan")
                .price(25000.00)
                .quantity(4)
                .build();
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(afterPurchase);

        // ACT
        VehicleResponse response = inventoryService.purchase("v-001");

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getQuantity()).isEqualTo(4); // decremented by 1
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("purchase: should throw RuntimeException when vehicle is out of stock (quantity = 0)")
    void purchase_shouldThrow_whenQuantityIsZero() {
        // ARRANGE
        when(vehicleRepository.findById("v-002")).thenReturn(Optional.of(outOfStockVehicle));

        // ACT + ASSERT
        assertThatThrownBy(() -> inventoryService.purchase("v-002"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("out of stock");

        // Critical: must never save when out of stock
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    @DisplayName("purchase: should throw RuntimeException when vehicle ID does not exist")
    void purchase_shouldThrow_whenVehicleNotFound() {
        // ARRANGE
        when(vehicleRepository.findById("bad-id")).thenReturn(Optional.empty());

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
        when(vehicleRepository.findById("v-001")).thenReturn(Optional.of(inStockVehicle));

        // After restock by 10, quantity = 5 + 10 = 15
        Vehicle afterRestock = Vehicle.builder()
                .id("v-001")
                .make("Toyota")
                .model("Camry")
                .category("Sedan")
                .price(25000.00)
                .quantity(15)
                .build();
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(afterRestock);

        // ACT
        RestockRequest request = new RestockRequest(10);
        VehicleResponse response = inventoryService.restock("v-001", request);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getQuantity()).isEqualTo(15); // 5 + 10
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("restock: should also work for a previously out-of-stock vehicle")
    void restock_shouldWork_forOutOfStockVehicle() {
        // ARRANGE
        when(vehicleRepository.findById("v-002")).thenReturn(Optional.of(outOfStockVehicle));

        Vehicle afterRestock = Vehicle.builder()
                .id("v-002")
                .make("Ford")
                .model("F-150")
                .category("Truck")
                .price(40000.00)
                .quantity(20)
                .build();
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(afterRestock);

        // ACT
        RestockRequest request = new RestockRequest(20);
        VehicleResponse response = inventoryService.restock("v-002", request);

        // ASSERT
        assertThat(response.getQuantity()).isEqualTo(20); // 0 + 20
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("restock: should throw RuntimeException when vehicle ID does not exist")
    void restock_shouldThrow_whenVehicleNotFound() {
        // ARRANGE
        when(vehicleRepository.findById("bad-id")).thenReturn(Optional.empty());

        // ACT + ASSERT
        RestockRequest request = new RestockRequest(5);
        assertThatThrownBy(() -> inventoryService.restock("bad-id", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Vehicle not found");

        verify(vehicleRepository, never()).save(any());
    }
}
