package com.tinyls.urlshortener.security;

import com.tinyls.urlshortener.model.AuthProvider;
import com.tinyls.urlshortener.model.Role;
import com.tinyls.urlshortener.model.User;
import com.tinyls.urlshortener.repository.UserRepository;
import com.tinyls.urlshortener.security.oauth2.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Custom OAuth2 user service that handles OAuth2 authentication.
 * Extends Spring Security's DefaultOAuth2UserService to provide custom user
 * processing.
 * 
 * This service is responsible for:
 * - Processing OAuth2 user information from providers (Google, GitHub)
 * - Creating or updating user accounts based on OAuth2 data
 * - Handling multi-provider authentication for the same email
 * - Converting OAuth2 user data to application user format
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * Loads and processes an OAuth2 user.
     * This method is called by Spring Security during OAuth2 authentication.
     * 
     * @param userRequest the OAuth2 user request
     * @return processed OAuth2User object
     * @throws OAuth2AuthenticationException if user processing fails
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.debug("Loading OAuth2 user from request type: {}", userRequest.getClass().getSimpleName());

        OAuth2User oAuth2User = super.loadUser(userRequest);
        try {
            log.info("Processing OAuth2 user: {}", oAuth2User.getAttributes());
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    /**
     * Processes the OAuth2 user information and creates or updates the user
     * account.
     * 
     * @param userRequest the OAuth2 user request
     * @param oAuth2User  the OAuth2 user information
     * @return processed OAuth2User object
     * @throws OAuth2AuthenticationException if email is missing or processing fails
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found in OAuth2 user");
        }

        User user = userRepository.findByEmail(email)
                .map(existingUser -> handleExistingUser(existingUser, provider, oAuth2User))
                .orElseGet(() -> registerNewUser(provider, oAuth2User));

        log.debug("Creating CustomOAuth2User for user: {}", user.getEmail());
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    /**
     * Handles an existing user logging in with OAuth2.
     * 
     * @param existingUser the existing user
     * @param newProvider  the new OAuth2 provider
     * @param oAuth2User   the OAuth2 user information
     * @return updated User object
     * @throws OAuth2AuthenticationException if authentication is not allowed
     */
    private User handleExistingUser(User existingUser, String newProvider, OAuth2User oAuth2User) {
        if (existingUser.getProvider() == AuthProvider.LOCAL) {
            throw new OAuth2AuthenticationException(
                    "Account exists with email/password. Please login with your password.");
        }

        if (existingUser.getProvider().name().equalsIgnoreCase(newProvider)) {
            return updateExistingUser(existingUser, oAuth2User);
        }

        log.info("User {} is logging in with {} (previously used {})",
                existingUser.getEmail(), newProvider, existingUser.getProvider());
        return updateExistingUser(existingUser, oAuth2User);
    }

    /**
     * Updates an existing user's information from OAuth2 data.
     * 
     * @param existingUser the existing user
     * @param oAuth2User   the OAuth2 user information
     * @return updated User object
     */
    private User updateExistingUser(User existingUser, OAuth2User oAuth2User) {
        log.debug("Updating existing user: {} with provider: {}", existingUser.getEmail(), existingUser.getProvider());
        existingUser.setName(oAuth2User.getAttribute("name"));
        String avatarUrl = getAvatarUrl(oAuth2User, existingUser.getProvider().name().toLowerCase());
        if (avatarUrl != null) {
            log.debug("Updating avatar URL for user {} from {} to {}",
                    existingUser.getEmail(), existingUser.getAvatarUrl(), avatarUrl);
            existingUser.setAvatarUrl(avatarUrl);
        }
        User savedUser = userRepository.save(existingUser);
        log.debug("Updated user saved with avatar URL: {}", savedUser.getAvatarUrl());
        return savedUser;
    }

    /**
     * Registers a new user from OAuth2 data.
     * 
     * @param provider   the OAuth2 provider
     * @param oAuth2User the OAuth2 user information
     * @return newly created User object
     */
    private User registerNewUser(String provider, OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = getProviderId(oAuth2User);
        String avatarUrl = getAvatarUrl(oAuth2User, provider);

        log.debug("Registering new user with email: {}, provider: {}, avatar URL: {}",
                email, provider, avatarUrl);

        User user = User.builder()
                .email(email)
                .name(name != null ? name : email.split("@")[0])
                .provider(AuthProvider.valueOf(provider.toUpperCase()))
                .providerId(providerId)
                .avatarUrl(avatarUrl)
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        log.debug("New user registered with avatar URL: {}", savedUser.getAvatarUrl());
        return savedUser;
    }

    /**
     * Extracts the avatar URL from OAuth2 user attributes based on the provider.
     * 
     * @param oAuth2User the OAuth2 user information
     * @param provider   the OAuth2 provider
     * @return the avatar URL or null if not found
     */
    private String getAvatarUrl(OAuth2User oAuth2User, String provider) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        log.debug("Extracting avatar URL for provider: {} with attributes: {}", provider, attributes);

        String avatarUrl = null;
        if ("github".equalsIgnoreCase(provider)) {
            avatarUrl = (String) attributes.get("avatar_url");
            log.debug("Extracted GitHub avatar URL: {}", avatarUrl);
        }

        if (avatarUrl == null) {
            log.warn("No avatar URL found for provider: {}", provider);
        }
        return avatarUrl;
    }

    /**
     * Extracts the provider-specific ID from OAuth2 user attributes.
     * 
     * @param oAuth2User the OAuth2 user information
     * @return provider-specific ID
     * @throws OAuth2AuthenticationException if provider ID is not found
     */
    private String getProviderId(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        if (attributes.containsKey("id")) {
            return String.valueOf(attributes.get("id")); // GitHub
        }
        throw new OAuth2AuthenticationException("Provider ID not found in OAuth2 user");
    }
}