package com.dealership.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler — catches exceptions thrown anywhere in the app
 * and converts them into clean, consistent JSON error responses.
 *
 * This prevents raw stack traces from leaking to API consumers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles RuntimeException — covers not-found, out-of-stock, duplicate email, bad login.
     * Maps exception messages to appropriate HTTP status codes.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        HttpStatus status;

        // Map exception messages to HTTP status codes
        if (message != null && message.contains("not found")) {
            status = HttpStatus.NOT_FOUND;             // 404
        } else if (message != null && message.contains("already registered")) {
            status = HttpStatus.CONFLICT;              // 409
        } else if (message != null && (message.contains("Invalid email or password")
                || message.contains("out of stock"))) {
            status = HttpStatus.BAD_REQUEST;           // 400
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
        }

        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(message)
                        .build()
        );
    }

    /**
     * Handles @Valid annotation failures on request bodies.
     * Returns 400 with a list of all field validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        // Collect all field error messages into one string
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Validation Failed")
                        .message(message)
                        .build()
        );
    }

    /**
     * Handles 403 Forbidden — triggered when non-admin accesses admin-only endpoints.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .error("Forbidden")
                        .message("You do not have permission to perform this action")
                        .build()
        );
    }
}
