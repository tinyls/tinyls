package com.tinyls.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested resource cannot be found.
 * This exception is used to indicate that a requested entity (e.g., URL, user)
 * does not exist in the system.
 * 
 * The exception is automatically mapped to HTTP 404 (Not Found) status code.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Creates a new exception with a direct error message.
     * 
     * @param message the error message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with a message indicating the resource was not found.
     * 
     * @param resourceName the type of resource that was not found (e.g., "URL",
     *                     "User")
     * @param identifier   the identifier used to search for the resource
     */
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s not found with identifier: %s", resourceName, identifier));
    }
}