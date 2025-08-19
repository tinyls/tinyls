package com.tinyls.urlshortener.dto.url;

import com.tinyls.urlshortener.dto.validation.ValidUrl;
import com.tinyls.urlshortener.dto.validation.ValidationGroups;
import com.tinyls.urlshortener.model.UrlStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data Transfer Object for URL update requests.
 * Designed to be extensible for future URL fields.
 * Only includes fields that should be updatable.
 * 
 * Current updatable fields:
 * - originalUrl: The target URL to redirect to
 * - status: The status of the URL (ACTIVE, INACTIVE)
 * 
 * Future extensible fields can be added here:
 * - customTitle: Custom title for the URL
 * - description: Description of the URL
 * - tags: Tags for categorization
 * - expirationDate: When the URL should expire
 * - passwordProtection: Password protection settings
 * - analyticsPreferences: Analytics configuration
 * - etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The original URL that is being shortened.
     * Must be a valid URL and not exceed 2048 characters.
     * Optional - if not provided, the existing value will be kept.
     */
    @Size(max = 2048, message = "URL must not exceed 2048 characters", groups = { ValidationGroups.Update.class })
    @ValidUrl(groups = { ValidationGroups.Update.class })
    private String originalUrl;

    /**
     * The status of the URL (ACTIVE, INACTIVE, etc.).
     * Optional - if not provided, the existing value will be kept.
     */
    private UrlStatus status;

    // Future extensible fields can be added here:
    // private String customTitle;
    // private String description;
    // private List<String> tags;
    // private LocalDateTime expirationDate;
    // private String passwordProtection;
    // private Map<String, Object> analyticsPreferences;
    // etc.
}
