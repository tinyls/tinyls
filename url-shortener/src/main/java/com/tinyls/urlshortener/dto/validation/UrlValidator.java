package com.tinyls.urlshortener.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Implementation of the URL validation logic for the {@link ValidUrl}
 * annotation.
 * Validates that a given string is a properly formatted URL.
 * 
 * The validator checks:
 * 1. The URL has a valid scheme (http, https)
 * 2. The URL has a valid host
 * 3. The URL follows proper URI syntax
 */
public class UrlValidator implements ConstraintValidator<ValidUrl, String> {

    /**
     * Initializes the validator.
     * 
     * @param constraintAnnotation the constraint annotation
     */
    @Override
    public void initialize(ValidUrl constraintAnnotation) {
        // No initialization needed
    }

    /**
     * Validates the URL string.
     * 
     * @param url     the URL string to validate
     * @param context the validation context
     * @return true if the URL is valid, false otherwise
     */
    @Override
    public boolean isValid(String url, ConstraintValidatorContext context) {
        if (url == null || url.trim().isEmpty()) {
            return true; // Let @NotNull handle null validation
        }

        try {
            URI uri = new URI(url);
            return uri.getScheme() != null &&
                    (uri.getScheme().equals("http") || uri.getScheme().equals("https")) &&
                    uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}