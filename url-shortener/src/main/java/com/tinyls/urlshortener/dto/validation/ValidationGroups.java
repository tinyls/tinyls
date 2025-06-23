package com.tinyls.urlshortener.dto.validation;

/**
 * Interface defining validation groups for different validation scenarios.
 * These groups allow for selective validation of fields based on the operation
 * being performed.
 */
public interface ValidationGroups {
    /**
     * Default validation group.
     * Used for basic validation that should always be performed.
     */
    interface Default {
    }

    /**
     * Validation group for create operations.
     * Used when validating data for creating new resources.
     * Typically includes all required fields and their format validation.
     */
    interface Create {
    }

    /**
     * Validation group for update operations.
     * Used when validating data for updating existing resources.
     * May have different requirements than create operations.
     */
    interface Update {
    }
}