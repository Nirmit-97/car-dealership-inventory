package com.dealership.auth;

import com.dealership.auth.dto.AuthResponse;
import com.dealership.auth.dto.LoginRequest;
import com.dealership.auth.dto.RegisterRequest;
import com.dealership.user.User;
import com.dealership.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service handling user registration and login.
 *
 * Responsibilities:
 *  - register(): validates uniqueness, hashes password, saves user, returns JWT
 *  - login():    validates credentials, returns JWT on success
 *
 * This class was implemented AFTER AuthServiceTest (TDD GREEN phase).
 */
@Service
@RequiredArgsConstructor    // Lombok: constructor injection for all final fields
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Register a new user.
     *
     * @param request RegisterRequest containing email and password
     * @return AuthResponse with JWT token, email, and role
     * @throws RuntimeException if email is already registered
     */
    public AuthResponse register(RegisterRequest request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Build user with hashed password — never store plain text
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        // Persist to DB
        User savedUser = userRepository.save(user);

        // Generate JWT for immediate login after registration
        String token = jwtUtil.generateToken(savedUser);

        return AuthResponse.builder()
                .token(token)
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .message("Registration successful")
                .build();
    }

    /**
     * Authenticate an existing user.
     *
     * @param request LoginRequest containing email and password
     * @return AuthResponse with JWT token, email, and role
     * @throws RuntimeException if credentials are invalid (generic message to prevent user enumeration)
     */
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Verify password against stored BCrypt hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Generate JWT
        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .message("Login successful")
                .build();
    }
}
