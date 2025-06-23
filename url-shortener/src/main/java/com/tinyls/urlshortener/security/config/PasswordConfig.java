package com.tinyls.urlshortener.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password Configuration class.
 * Configures the password encoding strategy for the application.
 * 
 * This configuration class provides a BCryptPasswordEncoder bean that is used
 * throughout the application for password hashing and verification. BCrypt is
 * a strong password hashing algorithm that automatically handles salt
 * generation
 * and provides protection against rainbow table attacks.
 * 
 * The default strength of BCrypt is 10, which provides a good balance between
 * security and performance. Each password hash includes the salt and the number
 * of rounds used, making it future-proof and allowing for strength upgrades.
 */
@Configuration
public class PasswordConfig {

    /**
     * Creates and configures the password encoder bean.
     * 
     * @return BCryptPasswordEncoder instance for password hashing and verification
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}