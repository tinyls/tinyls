package com.tinyls.urlshortener.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Base class for all API sub-errors.
 * Sub-errors provide detailed information about specific validation or error
 * conditions.
 * 
 * This class is abstract and should be extended by specific error types
 * like validation errors or field-specific errors.
 */
@Getter
@Setter
public abstract class ApiSubError {
    /**
     * The object that contains the error.
     * For example, the name of the form or entity.
     */
    private String object;

    /**
     * The field that contains the error.
     * For example, the name of the input field.
     */
    private String field;

    /**
     * The rejected value that caused the error.
     * For example, the invalid input value.
     */
    private Object rejectedValue;

    /**
     * A message explaining why the value was rejected.
     */
    private String message;

    /**
     * Creates a new ApiSubError with the specified details.
     * 
     * @param object        the object containing the error
     * @param field         the field containing the error
     * @param rejectedValue the value that was rejected
     * @param message       the error message
     */
    protected ApiSubError(String object, String field, Object rejectedValue, String message) {
        this.object = object;
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.message = message;
    }
}