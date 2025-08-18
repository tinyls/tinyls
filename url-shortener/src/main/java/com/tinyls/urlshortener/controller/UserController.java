package com.tinyls.urlshortener.controller;

import com.tinyls.urlshortener.dto.user.UserRequestDTO;
import com.tinyls.urlshortener.dto.user.UserResponseDTO;
import com.tinyls.urlshortener.dto.user.ProfileUpdateRequest;
import com.tinyls.urlshortener.security.UserDetailsAdapter;
import com.tinyls.urlshortener.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
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
@Tag(name = "Users", description = "User profile management operations")
public class UserController {

    private final UserService userService;

    /**
     * Updates the authenticated user's profile information.
     * 
     * @param userDetails The authenticated user's details
     * @param request     The profile update request containing new user details
     * @return The updated user details
     * @throws AccessDeniedException if the user is not authenticated
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request) {
        try {
            UUID userId = ((UserDetailsAdapter) userDetails).getUserId();
            log.info("Updating profile for user: {}", userId);

            UserRequestDTO userDTO = new UserRequestDTO(request.email(), null, request.name());
            return ResponseEntity.ok(userService.updateUser(userId, userDTO));
        } catch (Exception e) {
            log.error("Error updating profile for user: {}", userDetails.getUsername(), e);
            throw e; // Let GlobalExceptionHandler handle it
        }
    }

    /**
     * Deletes the authenticated user's account.
     * 
     * @param userDetails The authenticated user's details
     * @return No content response if deletion is successful
     * @throws AccessDeniedException if the user is not authenticated
     */
    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            UUID userId = ((UserDetailsAdapter) userDetails).getUserId();
            log.info("Deleting account for user: {}", userId);

            userService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting account for user: {}", userDetails.getUsername(), e);
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
}