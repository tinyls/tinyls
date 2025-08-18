package com.tinyls.urlshortener.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for profile updates.
 * 
 * This DTO is used when updating user profile information.
 * It contains the fields that can be updated in a user profile.
 */
public record ProfileUpdateRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email) {
}
