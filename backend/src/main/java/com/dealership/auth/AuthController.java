package com.dealership.auth;

import com.dealership.auth.dto.AuthResponse;
import com.dealership.auth.dto.LoginRequest;
import com.dealership.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints.
 *
 * Endpoints:
 *  POST /api/auth/register  — register a new user (public)
 *  POST /api/auth/login     — login and get JWT token (public)
 *
 * Both endpoints are public (no JWT required) — configured in SecurityConfig.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user account.
     *
     * Request body: { "email": "...", "password": "..." }
     * Response (201): { "token": "...", "email": "...", "role": "USER", "message": "..." }
     * Response (409): if email already registered
     * Response (400): if validation fails
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login with existing credentials to receive a JWT token.
     *
     * Request body: { "email": "...", "password": "..." }
     * Response (200): { "token": "...", "email": "...", "role": "USER"/"ADMIN", "message": "..." }
     * Response (400): if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
