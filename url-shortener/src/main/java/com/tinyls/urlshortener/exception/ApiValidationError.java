package com.tinyls.urlshortener.exception;

/**
 * Represents a validation error in the API.
 * This class extends ApiSubError to provide specific validation error
 * information.
 * 
 * Used to represent field-level validation errors, such as:
 * - Required fields that are missing
 * - Fields with invalid formats
 * - Fields with values outside allowed ranges
 */
public class ApiValidationError extends ApiSubError {
    /**
     * Creates a new validation error with the specified details.
     * 
     * @param object        the object containing the validation error
     * @param field         the field that failed validation
     * @param rejectedValue the invalid value that was rejected
     * @param message       the validation error message
     */
    public ApiValidationError(String object, String field, Object rejectedValue, String message) {
        super(object, field, rejectedValue, message);
    }
}