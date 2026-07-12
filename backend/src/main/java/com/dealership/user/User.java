package com.dealership.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a registered user.
 * Stored in the "users" table in PostgreSQL.
 *
 * Fields:
 *  - id       : UUID primary key (auto-generated)
 *  - email    : unique login identifier
 *  - password : BCrypt-hashed password (never stored as plain text)
 *  - role     : USER or ADMIN (stored as string in DB)
 *  - createdAt: timestamp of registration
 */
@Entity
@Table(name = "users")
@Data                   // Lombok: generates getters, setters, equals, hashCode, toString
@Builder                // Lombok: enables User.builder().email(...).build() pattern
@NoArgsConstructor      // Lombok: required by JPA
@AllArgsConstructor     // Lombok: required by @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
