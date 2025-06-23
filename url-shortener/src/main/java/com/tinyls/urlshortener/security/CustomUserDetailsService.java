package com.tinyls.urlshortener.security;

import com.tinyls.urlshortener.model.User;
import com.tinyls.urlshortener.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Custom UserDetailsService interface that extends Spring Security's
 * UserDetailsService.
 * Provides methods to load user details by email (username) or user ID.
 * 
 * This service is responsible for:
 * - Loading user details for authentication
 * - Converting User entities to UserDetails objects
 * - Handling user lookup by different identifiers
 */
public interface CustomUserDetailsService extends UserDetailsService {
    /**
     * Loads a user by their unique identifier.
     * 
     * @param id the user's UUID
     * @return UserDetails object containing the user's security information
     * @throws UsernameNotFoundException if the user is not found
     */
    UserDetails loadUserById(UUID id) throws UsernameNotFoundException;
}

/**
 * Implementation of CustomUserDetailsService.
 * Handles the actual loading of user details from the database.
 */
@Service
@RequiredArgsConstructor
class CustomUserDetailsServiceImpl implements CustomUserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by their email address.
     * This method is called by Spring Security during authentication.
     * 
     * @param email the user's email address
     * @return UserDetails object containing the user's security information
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new UserDetailsAdapter(
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                true,
                user.getId());
    }

    /**
     * Loads a user by their unique identifier.
     * This method is used for loading user details when the ID is known.
     * 
     * @param id the user's UUID
     * @return UserDetails object containing the user's security information
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return new UserDetailsAdapter(
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                true,
                user.getId());
    }
}