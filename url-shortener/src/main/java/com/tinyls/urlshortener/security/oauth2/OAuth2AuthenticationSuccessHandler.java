package com.tinyls.urlshortener.security.oauth2;

import com.tinyls.urlshortener.dto.user.OAuth2UserDTO;
import com.tinyls.urlshortener.dto.user.UserResponseDTO;
import com.tinyls.urlshortener.model.AuthProvider;
import com.tinyls.urlshortener.model.Role;
import com.tinyls.urlshortener.security.jwt.JwtTokenProvider;
import com.tinyls.urlshortener.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 Authentication Success Handler.
 * Handles successful OAuth2 authentication by:
 * - Processing OAuth2 user information
 * - Creating or retrieving user accounts
 * - Generating JWT tokens
 * - Redirecting to the frontend with authentication data
 * 
 * This handler extends SimpleUrlAuthenticationSuccessHandler to customize
 * the post-authentication behavior for OAuth2 login.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Value("${frontend.url}")
    private String frontendUrl;

    /**
     * Handles successful OAuth2 authentication.
     * The process includes:
     * 1. Extracting user information from OAuth2 response
     * 2. Creating or retrieving user account
     * 3. Generating JWT token
     * 4. Redirecting to frontend with token
     * 
     * @param request        the HTTP request
     * @param response       the HTTP response
     * @param authentication the authentication object containing OAuth2 user
     *                       details
     * @throws IOException      if redirect fails
     * @throws RuntimeException if required user information is missing
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauthToken.getPrincipal();

        log.debug("Processing OAuth2 authentication success for user: {}", oauth2User.getAttributes());

        // Get user email from OAuth2 user
        String email = oauth2User.getAttribute("email");
        if (email == null) {
            throw new RuntimeException("Email not found in OAuth2 user");
        }

        // Check if user exists, if not create one
        UserResponseDTO user;
        try {
            user = userService.getUserByEmail(email);
            log.debug("Found existing user: {}", user);
        } catch (Exception e) {
            // Create new user with OAuth2 details
            OAuth2UserDTO oauth2UserDTO = OAuth2UserDTO.builder()
                    .email(email)
                    .name(oauth2User.getAttribute("name"))
                    .provider(AuthProvider.valueOf(oauthToken.getAuthorizedClientRegistrationId().toUpperCase()))
                    .providerId(oauth2User.getAttribute("sub") != null ? oauth2User.getAttribute("sub")
                            : String.valueOf(oauth2User.getAttribute("id")))
                    .role(Role.USER)
                    .build();
            user = userService.createOAuth2User(oauth2UserDTO);
            log.debug("Created new user: {}", user);
        }

        // Create a new OAuth2User with the user ID
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
        attributes.put("user_id", user.getId());
        OAuth2User userWithId = new DefaultOAuth2User(oauth2User.getAuthorities(), attributes, "email");

        // Create a new authentication token with the updated user
        OAuth2AuthenticationToken newAuth = new OAuth2AuthenticationToken(
                userWithId,
                oauth2User.getAuthorities(),
                oauthToken.getAuthorizedClientRegistrationId());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(newAuth);
        log.debug("Generated JWT token for user: {}", user.getEmail());

        // Redirect to frontend with token
        String redirectUrl = String.format("%s/oauth2-callback?token=%s", frontendUrl, token);
        log.debug("Redirecting to: {}", redirectUrl);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}