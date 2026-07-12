package com.dealership.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response body returned for all API errors.
 * Ensures consistent error format across all endpoints.
 *
 * Example:
 * {
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Vehicle not found with id: abc",
 *   "timestamp": "2024-01-01T10:00:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private String error;
    private String message;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
