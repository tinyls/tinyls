package com.tinyls.urlshortener.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user creation and update requests.
 * Contains the basic user information required for registration or profile
 * updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {
    /**
     * The user's email address.
     * Must be a valid email format.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * The user's password.
     * Must be at least 8 characters long.
     * Note: This field is optional for profile updates.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    /**
     * The user's full name.
     * Must not exceed 100 characters.
     */
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
}