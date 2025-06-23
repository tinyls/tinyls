package com.tinyls.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts to access a resource without proper
 * authorization.
 * This exception is used to indicate that a user is authenticated but does not
 * have
 * the required permissions to access the requested resource.
 * 
 * The exception is automatically mapped to HTTP 403 (Forbidden) status code.
 * 
 * Common scenarios:
 * - User trying to access another user's data
 * - User trying to perform admin-only operations
 * - User trying to modify protected resources
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedException extends RuntimeException {
    /**
     * Creates a new exception with a message explaining the authorization failure.
     * 
     * @param message the detail message explaining why access was denied
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}