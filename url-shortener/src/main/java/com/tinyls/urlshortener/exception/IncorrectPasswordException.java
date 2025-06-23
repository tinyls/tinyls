package com.tinyls.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts to update their password with an
 * incorrect current password.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException() {
        super("Current password is incorrect");
    }
}