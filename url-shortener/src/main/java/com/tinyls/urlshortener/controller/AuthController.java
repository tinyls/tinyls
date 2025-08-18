package com.tinyls.urlshortener.controller;

import com.tinyls.urlshortener.dto.user.UserResponseDTO;
import com.tinyls.urlshortener.dto.user.PasswordUpdateDTO;
import com.tinyls.urlshortener.exception.AuthenticationException;
import com.tinyls.urlshortener.exception.IncorrectPasswordException;
import com.tinyls.urlshortener.security.UserDetailsAdapter;
import com.tinyls.urlshortener.security.jwt.JwtTokenProvider;
import com.tinyls.urlshortener.service.AuthService;
import com.tinyls.urlshortener.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Controller handling authentication-related operations including user
 * registration,
 * login, OAuth2 authentication, and password management.
 * 
 * All endpoints are prefixed with /api/auth
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication-related operations")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    /**
     * Registers a new user in the system.
     * 
     * @param request The registration request containing user details
     * @return A success message if registration is successful
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("Registering new user with email: {}", request.email());
            authService.registerUser(request.email(), request.password(), request.name());
            return ResponseEntity.ok(Map.of("message", "User registered successfully"));
        } catch (Exception e) {
            log.error("Error registering user with email: {}", request.email(), e);
            throw e; // Let GlobalExceptionHandler handle it
        }
    }

    /**
     * Authenticates a user and returns a JWT token.
     * 
     * @param request The login request containing user credentials
     * @return A JWT token if authentication is successful
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Authenticating user with email: {}", request.email());
            Authentication authentication = authService.authenticateUser(request.email(), request.password());
            String jwt = tokenProvider.generateToken(authentication);
            return ResponseEntity.ok(Map.of("token", jwt));
        } catch (Exception e) {
            log.error("Error authenticating user with email: {}", request.email(), e);
            throw e; // Let GlobalExceptionHandler handle it
        }
    }

    /**
     * Retrieves the current authenticated user's details.
     * 
     * @param userDetails The authenticated user's details
     * @return The user's details
     * @throws AccessDeniedException if the user is not authenticated
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            UUID userId = ((UserDetailsAdapter) userDetails).getUserId();
            log.debug("Retrieving details for user: {}", userId);
            return ResponseEntity.ok(userService.getUserById(userId));
        } catch (Exception e) {
            log.error("Error retrieving details for user: {}", userDetails.getUsername(), e);
            throw e; // Let GlobalExceptionHandler handle it
        }
    }

    /**
     * Handles the OAuth2 authentication success callback.
     * 
     * @param token The JWT token received from OAuth2 provider
     * @return The JWT token
     */
    @GetMapping("/oauth2/success")
    public ResponseEntity<?> oauth2Success(@RequestParam String token) {
        try {
            log.debug("OAuth2 authentication successful");
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            log.error("Error in OAuth2 success callback", e);
            throw e; // Let GlobalExceptionHandler handle it
        }
    }

    /**
     * Updates the authenticated user's password.
     * 
     * @param userDetails The authenticated user's details
     * @param passwordDTO The password update request
     * @return The updated user details
     * @throws AccessDeniedException      if the user is not authenticated
     * @throws IncorrectPasswordException if the current password is incorrect
     */
    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordUpdateDTO passwordDTO) {
        try {
            UUID userId = ((UserDetailsAdapter) userDetails).getUserId();
            log.info("Updating password for user: {}", userId);
            return ResponseEntity.ok(userService.updatePassword(userId, passwordDTO));
        } catch (Exception e) {
            log.error("Error updating password for user: {}", userDetails.getUsername(), e);
            throw e; // Let GlobalExceptionHandler handle it
        }
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
 * Request DTO for user registration.
 */
record RegisterRequest(
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,

        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters long") String password,

        @NotBlank(message = "Name is required") String name) {
}

/**
 * Request DTO for user login.
 */
record LoginRequest(
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,

        @NotBlank(message = "Password is required") String password) {
}