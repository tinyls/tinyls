package com.tinyls.urlshortener.controller;

import com.tinyls.urlshortener.dto.user.UserRequestDTO;
import com.tinyls.urlshortener.dto.user.UserResponseDTO;
import com.tinyls.urlshortener.exception.AuthenticationException;
import com.tinyls.urlshortener.security.UserDetailsAdapter;
import com.tinyls.urlshortener.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller handling user profile management operations.
 * 
 * All endpoints are prefixed with /api/users and require authentication.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Updates the authenticated user's profile information.
     * 
     * @param userDetails The authenticated user's details
     * @param request     The profile update request containing new user details
     * @return The updated user details
     * @throws AuthenticationException if the user is not authenticated
     */
    @PutMapping("/profile")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request) {
        validateUserDetails(userDetails);
        UUID userId = ((UserDetailsAdapter) userDetails).getUserId();
        log.info("Updating profile for user: {}", userId);

        UserRequestDTO userDTO = new UserRequestDTO(request.email(), null, request.name());
        return ResponseEntity.ok(userService.updateUser(userId, userDTO));
    }

    /**
     * Deletes the authenticated user's account.
     * 
     * @param userDetails The authenticated user's details
     * @return No content response if deletion is successful
     * @throws AuthenticationException if the user is not authenticated
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        validateUserDetails(userDetails);
        UUID userId = ((UserDetailsAdapter) userDetails).getUserId();
        log.info("Deleting account for user: {}", userId);

        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Validates that the user details are present and of the correct type.
     * 
     * @param userDetails The user details to validate
     * @throws AuthenticationException if validation fails
     */
    private void validateUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            throw new AuthenticationException("Unauthorized - Please login");
        }
        if (!(userDetails instanceof UserDetailsAdapter)) {
            throw new AuthenticationException("Invalid user details");
        }
    }
}

/**
 * Request DTO for profile updates.
 */
record ProfileUpdateRequest(
        @NotBlank(message = "Name is required") String name,

        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email) {
}