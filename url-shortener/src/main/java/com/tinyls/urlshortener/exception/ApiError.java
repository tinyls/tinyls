package com.tinyls.urlshortener.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a standardized API error response.
 * This class provides a consistent structure for all error responses from the
 * API.
 * 
 * The error response includes:
 * - Timestamp of when the error occurred
 * - HTTP status code
 * - Error message
 * - List of sub-errors (validation errors, etc.)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    /**
     * The timestamp when the error occurred.
     * Formatted as ISO-8601 date-time.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * The HTTP status code of the error.
     */
    private int status;

    /**
     * A brief error message describing what went wrong.
     */
    private String message;

    /**
     * A more detailed explanation of the error.
     */
    private String debugMessage;

    /**
     * List of sub-errors that provide more specific details about the error.
     * For example, validation errors for each field.
     */
    @Builder.Default
    private List<ApiSubError> subErrors = new ArrayList<>();

    /**
     * Creates a new ApiError with the specified status and message.
     * 
     * @param status  the HTTP status code
     * @param message the error message
     */
    public ApiError(int status, String message) {
        this();
        this.status = status;
        this.message = message;
    }

    /**
     * Creates a new ApiError with the specified status, message, and debug message.
     * 
     * @param status       the HTTP status code
     * @param message      the error message
     * @param debugMessage the detailed error message
     */
    public ApiError(int status, String message, String debugMessage) {
        this(status, message);
        this.debugMessage = debugMessage;
    }

    /**
     * Adds a sub-error to the list of sub-errors.
     * 
     * @param subError the sub-error to add
     */
    public void addSubError(ApiSubError subError) {
        if (subErrors == null) {
            subErrors = new ArrayList<>();
        }
        subErrors.add(subError);
    }
}