package com.dealership.integration;

import com.dealership.vehicle.Vehicle;
import com.dealership.vehicle.VehicleRepository;
import com.dealership.vehicle.VehicleRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for VehicleController and InventoryController.
 *
 * Uses MockMvc to verify security bindings (@PreAuthorize) and HTTP responses.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // uses H2 in-memory DB
class VehicleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleRepository vehicleRepository;

    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();

        testVehicle = Vehicle.builder()
                .make("Toyota")
                .model("Camry")
                .category("Sedan")
                .price(25000.0)
                .quantity(5)
                .build();
        
        testVehicle = vehicleRepository.save(testVehicle);
    }

    // =========================================================
    // SECURITY TESTS (Without Token)
    // =========================================================

    @Test
    @DisplayName("GET /api/vehicles - returns 403 Forbidden when unauthenticated")
    void getVehicles_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isForbidden());
    }

    // =========================================================
    // VEHICLE TESTS (With USER Role)
    // =========================================================

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/vehicles - success with USER role")
    void getVehicles_SuccessAsUser() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].make").value("Toyota"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/vehicles - success with USER role")
    void createVehicle_SuccessAsUser() throws Exception {
        VehicleRequest request = new VehicleRequest("Honda", "Civic", "Sedan", 22000.0, 10);

        mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.make").value("Honda"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/vehicles/{id} - returns 403 Forbidden with USER role")
    void deleteVehicle_ForbiddenAsUser() throws Exception {
        mockMvc.perform(delete("/api/vehicles/" + testVehicle.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/vehicles/{id}/restock - returns 403 Forbidden with USER role")
    void restockVehicle_ForbiddenAsUser() throws Exception {
        com.dealership.inventory.RestockRequest req = new com.dealership.inventory.RestockRequest();
        req.setAmount(5);

        mockMvc.perform(post("/api/vehicles/" + testVehicle.getId() + "/restock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/vehicles/{id}/purchase - success with USER role")
    void purchaseVehicle_SuccessAsUser() throws Exception {
        mockMvc.perform(post("/api/vehicles/" + testVehicle.getId() + "/purchase"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(4));
    }

    // =========================================================
    // VEHICLE TESTS (With ADMIN Role)
    // =========================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/vehicles/{id} - success with ADMIN role")
    void deleteVehicle_SuccessAsAdmin() throws Exception {
        mockMvc.perform(delete("/api/vehicles/" + testVehicle.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/vehicles/{id}/restock - success with ADMIN role")
    void restockVehicle_SuccessAsAdmin() throws Exception {
        com.dealership.inventory.RestockRequest req = new com.dealership.inventory.RestockRequest();
        req.setAmount(5);

        mockMvc.perform(post("/api/vehicles/" + testVehicle.getId() + "/restock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(10));
    }
}
