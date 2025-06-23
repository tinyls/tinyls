package com.tinyls.urlshortener.security.config;

import com.tinyls.urlshortener.security.CustomUserDetailsService;
import com.tinyls.urlshortener.security.CustomOAuth2UserService;
import com.tinyls.urlshortener.security.jwt.JwtAuthenticationFilter;
import com.tinyls.urlshortener.security.oauth2.CustomOidcUserService;
import com.tinyls.urlshortener.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.tinyls.urlshortener.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;

/**
 * Security Configuration class.
 * Configures the security settings for the application, including:
 * - Authentication and authorization
 * - JWT token handling
 * - OAuth2 integration
 * - CORS configuration
 * - Session management
 * - Security filters
 * 
 * This configuration enables:
 * - Stateless session management using JWT
 * - OAuth2 authentication with Google and GitHub
 * - Method-level security using @PreAuthorize annotations
 * - CORS support for frontend applications
 * - Public endpoints for authentication and URL redirection
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final CustomUserDetailsService userDetailsService;
        private final CustomOAuth2UserService oAuth2UserService;
        private final CustomOidcUserService oidcUserService;
        private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
        private final JwtTokenProvider tokenProvider;

        @Value("${frontend.url}")
        private String frontendUrl;

        /**
         * Creates and configures the JWT authentication filter.
         * This filter intercepts requests to validate JWT tokens.
         * 
         * @return JwtAuthenticationFilter instance
         */
        @Bean
        public JwtAuthenticationFilter jwtAuthenticationFilter() {
                return new JwtAuthenticationFilter(tokenProvider, userDetailsService);
        }

        /**
         * Creates and configures the authentication manager.
         * Used for handling authentication requests.
         * 
         * @param authConfig the authentication configuration
         * @return AuthenticationManager instance
         * @throws Exception if authentication manager creation fails
         */
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        /**
         * Configures the security filter chain.
         * Sets up:
         * - CORS configuration
         * - CSRF protection
         * - Session management
         * - Authorization rules
         * - OAuth2 login
         * - Exception handling
         * - JWT filter
         * 
         * @param http the HttpSecurity instance
         * @return SecurityFilterChain instance
         * @throws Exception if filter chain configuration fails
         */
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api/auth/**", // Authentication endpoints
                                                                "/oauth2/**", // OAuth2 endpoints
                                                                "/login/oauth2/**", // OAuth2 callback
                                                                "/api/urls/**", // URL endpoints
                                                                "/api/urls/r/**", // URL redirection
                                                                "/api-docs/**", // API documentation
                                                                "/swagger-ui/**", // Swagger UI
                                                                "/swagger-ui.html", // Swagger UI entry point
                                                                "/actuator/**", // Actuator endpoints
                                                                "/actuator/health/**" // Health check endpoints
                                                ).permitAll()
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth2 -> oauth2
                                                .authorizationEndpoint(authorization -> authorization
                                                                .baseUri("/oauth2/authorize"))
                                                .redirectionEndpoint(redirection -> redirection
                                                                .baseUri("/login/oauth2/code/*"))
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .oidcUserService(oidcUserService)
                                                                .userService(oAuth2UserService))
                                                .successHandler(oAuth2AuthenticationSuccessHandler))
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                                        response.setContentType("application/json");
                                                        response.getWriter().write(
                                                                        "{\"status\":\"UNAUTHORIZED\",\"code\":\"UNAUTHORIZED\","
                                                                                        +
                                                                                        "\"message\":\"Authentication required\","
                                                                                        +
                                                                                        "\"debugMessage\":\"No authentication token provided\"}");
                                                }))
                                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        /**
         * Configures CORS settings for the application.
         * Uses the frontend.url property from application.properties to determine
         * allowed origins.
         * 
         * @return CorsConfigurationSource instance
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Collections.singletonList(frontendUrl));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}