package com.tinyls.urlshortener.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for validating URLs.
 * Ensures that the provided string is a valid URL format.
 * 
 * Usage:
 * {@code
 * @ValidUrl
 * private String url;
 * }
 */
@Documented
@Constraint(validatedBy = UrlValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUrl {
    /**
     * The error message to be displayed when validation fails.
     * 
     * @return the error message
     */
    String message() default "Invalid URL format";

    /**
     * The validation groups this constraint belongs to.
     * 
     * @return the validation groups
     */
    Class<?>[] groups() default {};

    /**
     * Additional metadata for the constraint.
     * 
     * @return the payload
     */
    Class<? extends Payload>[] payload() default {};
}