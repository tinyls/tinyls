package com.tinyls.urlshortener.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC Configuration class.
 * Configures Spring MVC settings for the application.
 * 
 * This configuration class enables Spring MVC and provides a place to customize
 * web-related configurations. Currently, CORS configuration has been moved to
 * SecurityConfig for better security management.
 * 
 * @see SecurityConfig
 */
@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {
    // Note: CORS configuration has been moved to SecurityConfig for better security
    // management
    // This class is kept for future web-related configurations

    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    // registry.addMapping("/**")
    // .allowedOrigins("*")
    // .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    // .allowedHeaders("*")
    // .exposedHeaders("Authorization");
    // }
}