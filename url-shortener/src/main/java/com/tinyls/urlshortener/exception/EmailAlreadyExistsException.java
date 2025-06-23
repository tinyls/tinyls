package com.tinyls.urlshortener.exception;

/**
 * Exception thrown when attempting to register a user with an email address
 * that is already registered in the system.
 * 
 * This exception is used to prevent duplicate user accounts and ensure
 * email addresses remain unique across the system.
 */
public class EmailAlreadyExistsException extends RuntimeException {
    /**
     * Creates a new exception with a message indicating the email is already in
     * use.
     * 
     * @param email the email address that already exists
     */
    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + email);
    }
}