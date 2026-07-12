package com.dealership.auth;

import com.dealership.auth.dto.AuthResponse;
import com.dealership.auth.dto.LoginRequest;
import com.dealership.auth.dto.RegisterRequest;
import com.dealership.user.Role;
import com.dealership.user.User;
import com.dealership.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TDD Unit Tests for AuthService.
 *
 * RED phase: These tests are written BEFORE AuthService is implemented.
 * They will FAIL initially — that is expected and correct TDD behavior.
 *
 * Tests use Mockito to isolate AuthService from the database and JWT.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // ===== Mocks — fake collaborators, no real DB or JWT calls =====
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    // ===== System Under Test =====
    @InjectMocks
    private AuthService authService;

    // =========================================================
    // REGISTER TESTS
    // =========================================================

    @Test
    @DisplayName("register: should hash the password and save user, return JWT token")
    void register_shouldHashPasswordAndSaveUser() {
        // ARRANGE
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");

        // Mock: email does not exist yet
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        // Mock: password encoder returns a hashed string
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        // Mock: save() returns a saved User with an ID
        User savedUser = User.builder()
                .id("uuid-123")
                .email("test@example.com")
                .password("hashed_password")
                .role(Role.USER)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        // Mock: JWT generation returns a token string
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt.token.here");

        // ACT
        AuthResponse response = authService.register(request);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt.token.here");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo(Role.USER);

        // Verify password was hashed — never stored plain text
        verify(passwordEncoder).encode("password123");
        // Verify user was saved exactly once
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register: should throw RuntimeException when email already exists")
    void register_shouldThrowException_whenEmailAlreadyExists() {
        // ARRANGE
        RegisterRequest request = new RegisterRequest("existing@example.com", "password123");

        // Mock: email already taken
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already registered");

        // Verify we never tried to save or hash when email is duplicate
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    // =========================================================
    // LOGIN TESTS
    // =========================================================

    @Test
    @DisplayName("login: should return JWT token when credentials are valid")
    void login_shouldReturnToken_whenCredentialsAreValid() {
        // ARRANGE
        LoginRequest request = new LoginRequest("user@example.com", "correctPassword");

        User existingUser = User.builder()
                .id("uuid-456")
                .email("user@example.com")
                .password("hashed_password")
                .role(Role.USER)
                .build();

        // Mock: user found by email
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));
        // Mock: password matches
        when(passwordEncoder.matches("correctPassword", "hashed_password")).thenReturn(true);
        // Mock: JWT returned
        when(jwtUtil.generateToken(existingUser)).thenReturn("valid.jwt.token");

        // ACT
        AuthResponse response = authService.login(request);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("valid.jwt.token");
        assertThat(response.getEmail()).isEqualTo("user@example.com");
        assertThat(response.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("login: should throw RuntimeException when email is not found")
    void login_shouldThrowException_whenEmailNotFound() {
        // ARRANGE
        LoginRequest request = new LoginRequest("ghost@example.com", "anyPassword");

        // Mock: no user with this email
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");

        // Verify we never checked the password for non-existent user
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("login: should throw RuntimeException when password is incorrect")
    void login_shouldThrowException_whenPasswordIsWrong() {
        // ARRANGE
        LoginRequest request = new LoginRequest("user@example.com", "wrongPassword");

        User existingUser = User.builder()
                .id("uuid-456")
                .email("user@example.com")
                .password("hashed_password")
                .role(Role.USER)
                .build();

        // Mock: user found
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));
        // Mock: password does NOT match
        when(passwordEncoder.matches("wrongPassword", "hashed_password")).thenReturn(false);

        // ACT + ASSERT
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");

        // Verify JWT was never generated for failed login
        verify(jwtUtil, never()).generateToken(any());
    }
}
