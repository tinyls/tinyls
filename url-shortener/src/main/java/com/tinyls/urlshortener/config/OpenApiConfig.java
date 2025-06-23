package com.tinyls.urlshortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) Configuration class.
 * Configures the API documentation for the URL Shortener service.
 * 
 * This configuration class sets up the OpenAPI documentation with:
 * - API information (title, description, version)
 * - Contact information for support
 * - License information
 * - Server configurations for different environments
 * 
 * The documentation is accessible at /swagger-ui.html when the application is
 * running.
 */
@Configuration
public class OpenApiConfig {

        /**
         * Creates and configures the OpenAPI documentation bean.
         * 
         * @return OpenAPI object containing the API documentation configuration
         */
        @Bean
        public OpenAPI urlShortenerOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("URL Shortener API")
                                                .description("RESTful API for URL shortening service. Provides endpoints for URL shortening, "
                                                                +
                                                                "redirection, user management, and analytics.")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("tinyls Team")
                                                                .email("support@tinyls.com")
                                                                .url("https://tinyls.com"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:8000")
                                                                .description("Local Development Server"),
                                                new Server()
                                                                .url("https://api.tinyls.com")
                                                                .description("Production Server")));
        }
}