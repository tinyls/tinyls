package com.tinyls.urlshortener.controller;

import com.tinyls.urlshortener.dto.user.UserResponseDTO;
import com.tinyls.urlshortener.dto.user.PasswordUpdateDTO;
import com.tinyls.urlshortener.exception.AuthenticationException;
import com.tinyls.urlshortener.exception.IncorrectPasswordException;
import com.tinyls.urlshortener.security.UserDetailsAdapter;
import com.tinyls.urlshortener.security.jwt.JwtTokenProvider;
import com.tinyls.urlshortener.service.AuthService;
import com.tinyls.urlshortener.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

// TODO: authentication protection and proper exception handling have to be implemented for returning correct status codes
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
        log.info("Registering new user with email: {}", request.email());
        authService.registerUser(request.email(), request.password(), request.name());
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    /**
     * Authenticates a user and returns a JWT token.
     * 
     * @param request The login request containing user credentials
     * @return A JWT token if authentication is successful
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest request) {
        log.info("Authenticating user with email: {}", request.email());
        Authentication authentication = authService.authenticateUser(request.email(), request.password());
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(Map.of("token", jwt));
    }

    /**
     * Retrieves the current authenticated user's details.
     * 
     * @param userDetails The authenticated user's details
     * @return The user's details
     * @throws AuthenticationException if the user is not authenticated
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        validateUserDetails(userDetails);
        UUID userId = ((UserDetailsAdapter) userDetails).getUserId();
        log.debug("Retrieving details for user: {}", userId);
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    /**
     * Handles the OAuth2 authentication success callback.
     * 
     * @param token The JWT token received from OAuth2 provider
     * @return The JWT token
     */
    @GetMapping("/oauth2/success")
    public ResponseEntity<?> oauth2Success(@RequestParam String token) {
        log.debug("OAuth2 authentication successful");
        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * Updates the authenticated user's password.
     * 
     * @param userDetails The authenticated user's details
     * @param passwordDTO The password update request
     * @return The updated user details
     * @throws AuthenticationException    if the user is not authenticated
     * @throws IncorrectPasswordException if the current password is incorrect
     */
    @PutMapping("/password")
    public ResponseEntity<UserResponseDTO> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordUpdateDTO passwordDTO) {
        validateUserDetails(userDetails);
        UUID userId = ((UserDetailsAdapter) userDetails).getUserId();
        log.info("Updating password for user: {}", userId);
        return ResponseEntity.ok(userService.updatePassword(userId, passwordDTO));
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