package com.tinyls.urlshortener.exception;

/**
 * Exception thrown when a password fails to meet strength requirements.
 * This exception is used to indicate that a password does not meet the
 * minimum security requirements set by the application.
 */
public class PasswordValidationException extends RuntimeException {

    public PasswordValidationException(String message) {
        super(message);
    }
}