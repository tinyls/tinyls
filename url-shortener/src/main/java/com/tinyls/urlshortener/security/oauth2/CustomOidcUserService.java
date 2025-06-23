package com.tinyls.urlshortener.security.oauth2;

import com.tinyls.urlshortener.model.AuthProvider;
import com.tinyls.urlshortener.model.Role;
import com.tinyls.urlshortener.model.User;
import com.tinyls.urlshortener.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
    private final OidcUserService delegate = new OidcUserService();
    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        log.debug("Loading OIDC user for provider: {}", userRequest.getClientRegistration().getRegistrationId());

        // 1) Let Spring load the IdToken & UserInfo
        OidcUser oidcUser = delegate.loadUser(userRequest);

        try {
            // 2) Map/create your local User entity
            String email = oidcUser.getEmail();
            User user = userRepository.findByEmail(email)
                    .map(existingUser -> handleExistingUser(existingUser,
                            userRequest.getClientRegistration().getRegistrationId(), oidcUser))
                    .orElseGet(() -> registerNewOidcUser(userRequest, oidcUser));

            // 3) Wrap into your CustomOidcUser
            log.debug("Creating CustomOidcUser for user: {}", user.getEmail());
            return new CustomOidcUser(user, oidcUser);
        } catch (Exception ex) {
            log.error("Error processing OIDC user", ex);
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private User handleExistingUser(User existingUser, String provider, OidcUser oidcUser) {
        if (existingUser.getProvider() == AuthProvider.LOCAL) {
            throw new OAuth2AuthenticationException(
                    "Account exists with email/password. Please login with your password.");
        }

        if (existingUser.getProvider().name().equalsIgnoreCase(provider)) {
            return updateExistingUser(existingUser, oidcUser);
        }

        log.info("User {} is logging in with {} (previously used {})",
                existingUser.getEmail(), provider, existingUser.getProvider());
        return updateExistingUser(existingUser, oidcUser);
    }

    private User updateExistingUser(User existingUser, OidcUser oidcUser) {
        log.debug("Updating existing user: {} with provider: {}", existingUser.getEmail(), existingUser.getProvider());
        existingUser.setName(oidcUser.getFullName());
        String avatarUrl = oidcUser.getPicture();
        if (avatarUrl != null) {
            log.debug("Updating avatar URL for user {} from {} to {}",
                    existingUser.getEmail(), existingUser.getAvatarUrl(), avatarUrl);
            existingUser.setAvatarUrl(avatarUrl);
        }
        User savedUser = userRepository.save(existingUser);
        log.debug("Updated user saved with avatar URL: {}", savedUser.getAvatarUrl());
        return savedUser;
    }

    private User registerNewOidcUser(OidcUserRequest req, OidcUser oidcUser) {
        log.debug("Registering new OIDC user with email: {}", oidcUser.getEmail());

        User user = User.builder()
                .email(oidcUser.getEmail())
                .name(oidcUser.getFullName())
                .provider(AuthProvider.valueOf(req.getClientRegistration().getRegistrationId().toUpperCase()))
                .providerId(oidcUser.getSubject())
                .avatarUrl(oidcUser.getPicture())
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        log.debug("New OIDC user registered with avatar URL: {}", savedUser.getAvatarUrl());
        return savedUser;
    }
}