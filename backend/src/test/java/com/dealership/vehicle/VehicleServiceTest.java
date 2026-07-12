package com.dealership.vehicle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TDD Unit Tests for VehicleService.
 *
 * RED phase: Written BEFORE VehicleService exists.
 * Expected to FAIL until VehicleService is implemented (GREEN phase).
 *
 * Covers:
 *  - createVehicle()
 *  - getAllVehicles()
 *  - getVehicleById()
 *  - searchVehicles()
 *  - updateVehicle()
 *  - deleteVehicle()
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    // Reusable test fixtures
    private Vehicle sampleVehicle;
    private VehicleRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleVehicle = Vehicle.builder()
                .id("v-uuid-001")
                .make("Toyota")
                .model("Camry")
                .category("Sedan")
                .price(25000.00)
                .quantity(10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleRequest = new VehicleRequest("Toyota", "Camry", "Sedan", 25000.00, 10);
    }

    // =========================================================
    // CREATE VEHICLE
    // =========================================================

    @Test
    @DisplayName("createVehicle: should persist vehicle and return VehicleResponse")
    void createVehicle_shouldSaveAndReturnResponse() {
        // ARRANGE
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(sampleVehicle);

        // ACT
        VehicleResponse response = vehicleService.createVehicle(sampleRequest);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("v-uuid-001");
        assertThat(response.getMake()).isEqualTo("Toyota");
        assertThat(response.getModel()).isEqualTo("Camry");
        assertThat(response.getCategory()).isEqualTo("Sedan");
        assertThat(response.getPrice()).isEqualTo(25000.00);
        assertThat(response.getQuantity()).isEqualTo(10);

        // Verify repository was called exactly once
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    // =========================================================
    // GET ALL VEHICLES
    // =========================================================

    @Test
    @DisplayName("getAllVehicles: should return a list of all vehicles")
    void getAllVehicles_shouldReturnListOfResponses() {
        // ARRANGE
        Vehicle second = Vehicle.builder()
                .id("v-uuid-002")
                .make("Ford")
                .model("Mustang")
                .category("Coupe")
                .price(45000.00)
                .quantity(5)
                .build();

        when(vehicleRepository.findAll()).thenReturn(List.of(sampleVehicle, second));

        // ACT
        List<VehicleResponse> result = vehicleService.getAllVehicles();

        // ASSERT
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMake()).isEqualTo("Toyota");
        assertThat(result.get(1).getMake()).isEqualTo("Ford");
    }

    @Test
    @DisplayName("getAllVehicles: should return empty list when no vehicles exist")
    void getAllVehicles_shouldReturnEmptyList_whenNoneExist() {
        // ARRANGE
        when(vehicleRepository.findAll()).thenReturn(List.of());

        // ACT
        List<VehicleResponse> result = vehicleService.getAllVehicles();

        // ASSERT
        assertThat(result).isEmpty();
    }

    // =========================================================
    // GET VEHICLE BY ID
    // =========================================================

    @Test
    @DisplayName("getVehicleById: should return vehicle when ID exists")
    void getVehicleById_shouldReturnResponse_whenFound() {
        // ARRANGE
        when(vehicleRepository.findById("v-uuid-001")).thenReturn(Optional.of(sampleVehicle));

        // ACT
        VehicleResponse response = vehicleService.getVehicleById("v-uuid-001");

        // ASSERT
        assertThat(response.getId()).isEqualTo("v-uuid-001");
        assertThat(response.getMake()).isEqualTo("Toyota");
    }

    @Test
    @DisplayName("getVehicleById: should throw RuntimeException when ID not found")
    void getVehicleById_shouldThrow_whenNotFound() {
        // ARRANGE
        when(vehicleRepository.findById("bad-id")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> vehicleService.getVehicleById("bad-id"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Vehicle not found");
    }

    // =========================================================
    // SEARCH VEHICLES
    // =========================================================

    @Test
    @DisplayName("searchVehicles: should return filtered results using Specification")
    void searchVehicles_shouldReturnFilteredResults() {
        // ARRANGE — Specification-based findAll() is mocked
        when(vehicleRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(sampleVehicle));

        // ACT
        List<VehicleResponse> result = vehicleService.searchVehicles("Toyota", null, null, null, null);

        // ASSERT
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMake()).isEqualTo("Toyota");
    }

    @Test
    @DisplayName("searchVehicles: should return empty list when no matches found")
    void searchVehicles_shouldReturnEmpty_whenNoMatch() {
        // ARRANGE
        when(vehicleRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of());

        // ACT
        List<VehicleResponse> result = vehicleService.searchVehicles("BMW", null, null, null, null);

        // ASSERT
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("searchVehicles: should filter by price range")
    void searchVehicles_shouldFilterByPriceRange() {
        // ARRANGE
        when(vehicleRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(sampleVehicle));

        // ACT
        List<VehicleResponse> result = vehicleService.searchVehicles(null, null, null, 20000.0, 30000.0);

        // ASSERT
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrice()).isBetween(20000.0, 30000.0);
    }

    // =========================================================
    // UPDATE VEHICLE
    // =========================================================

    @Test
    @DisplayName("updateVehicle: should update fields and return updated VehicleResponse")
    void updateVehicle_shouldUpdateAndReturn() {
        // ARRANGE
        VehicleRequest updateRequest = new VehicleRequest("Toyota", "Camry", "Sedan", 27000.00, 8);

        Vehicle updatedVehicle = Vehicle.builder()
                .id("v-uuid-001")
                .make("Toyota")
                .model("Camry")
                .category("Sedan")
                .price(27000.00)
                .quantity(8)
                .build();

        when(vehicleRepository.findById("v-uuid-001")).thenReturn(Optional.of(sampleVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(updatedVehicle);

        // ACT
        VehicleResponse response = vehicleService.updateVehicle("v-uuid-001", updateRequest);

        // ASSERT
        assertThat(response.getPrice()).isEqualTo(27000.00);
        assertThat(response.getQuantity()).isEqualTo(8);
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("updateVehicle: should throw RuntimeException when vehicle not found")
    void updateVehicle_shouldThrow_whenNotFound() {
        // ARRANGE
        when(vehicleRepository.findById("bad-id")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> vehicleService.updateVehicle("bad-id", sampleRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Vehicle not found");

        verify(vehicleRepository, never()).save(any());
    }

    // =========================================================
    // DELETE VEHICLE
    // =========================================================

    @Test
    @DisplayName("deleteVehicle: should delete vehicle when ID exists")
    void deleteVehicle_shouldDelete_whenFound() {
        // ARRANGE
        when(vehicleRepository.findById("v-uuid-001")).thenReturn(Optional.of(sampleVehicle));
        doNothing().when(vehicleRepository).delete(sampleVehicle);

        // ACT — should not throw
        assertThatNoException().isThrownBy(() -> vehicleService.deleteVehicle("v-uuid-001"));

        // Verify delete was called exactly once
        verify(vehicleRepository, times(1)).delete(sampleVehicle);
    }

    @Test
    @DisplayName("deleteVehicle: should throw RuntimeException when vehicle not found")
    void deleteVehicle_shouldThrow_whenNotFound() {
        // ARRANGE
        when(vehicleRepository.findById("bad-id")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> vehicleService.deleteVehicle("bad-id"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Vehicle not found");

        verify(vehicleRepository, never()).delete(any(Vehicle.class));
    }
}
