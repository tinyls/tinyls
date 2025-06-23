package com.tinyls.urlshortener.security.jwt;

import com.tinyls.urlshortener.security.UserDetailsAdapter;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Token Provider for handling JWT operations.
 * This class is responsible for:
 * - Generating JWT tokens for authenticated users
 * - Validating JWT tokens
 * - Extracting user information from tokens
 * 
 * The provider supports both local authentication and OAuth2 authentication
 * by handling different types of authentication principals.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private int jwtExpirationInMs;

    private Key key;

    /**
     * Initializes the JWT signing key using the secret from properties.
     * This method is called after dependency injection is complete.
     */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generates a JWT token for an authenticated user.
     * Supports both local authentication and OAuth2 authentication.
     * 
     * @param authentication the authentication object containing user details
     * @return the generated JWT token
     * @throws RuntimeException if the authentication principal type is not
     *                          supported
     *                          or if user ID is not found
     */
    public String generateToken(Authentication authentication) {
        UUID userId;
        String email;

        if (authentication.getPrincipal() instanceof UserDetailsAdapter) {
            UserDetailsAdapter userDetails = (UserDetailsAdapter) authentication.getPrincipal();
            userId = userDetails.getUserId();
            email = userDetails.getUsername();
        } else if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            userId = (UUID) oauth2User.getAttribute("user_id");
            email = oauth2User.getAttribute("email");
        } else {
            throw new RuntimeException("Unsupported authentication principal type");
        }

        if (userId == null) {
            throw new RuntimeException("User ID not found in authentication");
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Extracts the user ID from a JWT token.
     * 
     * @param token the JWT token
     * @return the user ID from the token
     * @throws JwtException if the token is invalid or cannot be parsed
     */
    public UUID getUserIdFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }

    /**
     * Validates a JWT token.
     * Checks for:
     * - Valid signature
     * - Valid token format
     * - Token expiration
     * - Token support
     * - Non-empty claims
     * 
     * @param authToken the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }
}