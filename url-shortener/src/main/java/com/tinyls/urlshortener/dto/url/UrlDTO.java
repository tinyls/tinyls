package com.tinyls.urlshortener.dto.url;

import com.tinyls.urlshortener.dto.validation.ValidUrl;
import com.tinyls.urlshortener.dto.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Data Transfer Object for URL operations.
 * Represents a shortened URL with its metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlDTO {
        /**
         * The unique identifier of the URL.
         */
        private Long id;

        /**
         * The short code used to access the URL.
         * Must be 4-8 characters long and contain only letters, numbers, underscores,
         * and hyphens.
         */
        @Pattern(regexp = "^[A-Za-z0-9_-]{3,8}$", message = "Short code must be 3-8 characters long and contain only letters, numbers, underscores, and hyphens", groups = {
                        ValidationGroups.Default.class })
        private String shortCode;

        /**
         * The original URL that is being shortened.
         * Must be a valid URL and not exceed 2048 characters.
         */
        @NotBlank(message = "Original URL is required", groups = { ValidationGroups.Create.class,
                        ValidationGroups.Default.class })
        @Size(max = 2048, message = "URL must not exceed 2048 characters", groups = { ValidationGroups.Create.class,
                        ValidationGroups.Default.class })
        @ValidUrl(groups = { ValidationGroups.Create.class, ValidationGroups.Default.class })
        private String originalUrl;

        /**
         * The timestamp when the URL was created.
         */
        private Timestamp createdAt;

        /**
         * The number of times the URL has been accessed.
         */
        private Long clicks;

        /**
         * The ID of the user who created the URL.
         * Optional for anonymous URLs.
         */
        private UUID userId;
}