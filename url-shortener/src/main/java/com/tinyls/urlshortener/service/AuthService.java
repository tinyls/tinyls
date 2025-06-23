package com.tinyls.urlshortener.service;

import com.tinyls.urlshortener.exception.EmailAlreadyExistsException;
import com.tinyls.urlshortener.model.AuthProvider;
import com.tinyls.urlshortener.model.Role;
import com.tinyls.urlshortener.model.User;
import com.tinyls.urlshortener.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling authentication-related operations.
 * Provides functionality for user registration and authentication.
 * 
 * This service is responsible for:
 * - User registration with email/password
 * - User authentication
 * - Managing authentication state
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    /**
     * Registers a new user in the system.
     * 
     * @param email    The user's email address
     * @param password The user's password (will be encoded)
     * @param name     The user's name
     * @return The registered user
     * @throws EmailAlreadyExistsException if a user with the given email already
     *                                     exists
     * @throws IllegalArgumentException    if the password does not meet strength
     *                                     requirements
     */
    @Transactional
    public User registerUser(String email, String password, String name) {
        log.info("Registering new user with email: {}", email);

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        userService.validatePasswordStrength(password);

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .name(name)
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

    /**
     * Authenticates a user with the provided credentials.
     * 
     * @param email    The user's email address
     * @param password The user's password
     * @return The authentication object containing the user's details
     * @throws BadCredentialsException if the credentials are invalid
     */
    public Authentication authenticateUser(String email, String password) {
        log.debug("Authenticating user with email: {}", email);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return authentication;
        } catch (BadCredentialsException e) {
            log.warn("Authentication failed for user: {}", email);
            throw new BadCredentialsException("Invalid email or password");
        }
    }
}