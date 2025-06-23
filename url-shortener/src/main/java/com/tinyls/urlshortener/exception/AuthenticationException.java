package com.tinyls.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when authentication fails.
 * This exception is used to indicate that a user's credentials are invalid
 * or that the user is not authenticated.
 * 
 * Common scenarios:
 * - Invalid username/password
 * - Expired authentication token
 * - Missing authentication credentials
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticationException extends RuntimeException {
    /**
     * Creates a new authentication exception with the specified message.
     * 
     * @param message the detail message explaining the authentication failure
     */
    public AuthenticationException(String message) {
        super(message);
    }
}