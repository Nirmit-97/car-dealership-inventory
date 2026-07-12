package com.dealership.integration;

import com.dealership.auth.dto.LoginRequest;
import com.dealership.auth.dto.RegisterRequest;
import com.dealership.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 *
 * Uses MockMvc to test the actual HTTP endpoints, JSON serialization,
 * and Spring Security filter chains without starting a real Tomcat server.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // uses H2 in-memory database
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Clear database before each test to ensure isolation
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/register - success returns token and 201")
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("newuser@dealer.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("newuser@dealer.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /api/auth/login - success returns token and 200")
    void testLogin_Success() throws Exception {
        // First register a user
        RegisterRequest registerReq = new RegisterRequest("loginuser@dealer.com", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        // Then attempt login
        LoginRequest loginReq = new LoginRequest("loginuser@dealer.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("loginuser@dealer.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login - fails with bad credentials")
    void testLogin_BadCredentials() throws Exception {
        LoginRequest loginReq = new LoginRequest("wrong@dealer.com", "wrongpass");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
