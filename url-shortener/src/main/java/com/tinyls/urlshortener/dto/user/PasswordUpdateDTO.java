package com.tinyls.urlshortener.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for password update requests.
 * Contains the current password and the new password to be set.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordUpdateDTO {
    /**
     * The user's current password.
     * Required to verify the user's identity.
     */
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    /**
     * The new password to be set.
     * Must be at least 8 characters long.
     * Additional password strength requirements are validated in the service layer.
     */
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters long")
    private String newPassword;
}