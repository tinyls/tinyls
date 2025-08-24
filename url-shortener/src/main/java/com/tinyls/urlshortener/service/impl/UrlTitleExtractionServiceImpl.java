package com.tinyls.urlshortener.service.impl;

import com.tinyls.urlshortener.service.UrlTitleExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Implementation of URL title extraction service.
 * Uses HTTP client to fetch HTML content and extract title tags.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UrlTitleExtractionServiceImpl implements UrlTitleExtractionService {

    private final RestTemplate restTemplate;
    private final Executor asyncExecutor;

    @Value("${url.title.extraction.enabled:true}")
    private boolean titleExtractionEnabled;

    @Value("${url.title.extraction.timeout:5000}")
    private int timeoutMs;

    @Value("${url.title.extraction.max-content-length:50000}")
    private int maxContentLength;

    @Value("${url.title.extraction.fallback-to-description:false}")
    private boolean fallbackToDescription;

    @Value("${url.title.extraction.use-open-graph:false}")
    private boolean useOpenGraph;

    @Value("${url.title.extraction.user-agent:tinyls-URL-Title-Extractor/1.0}")
    private String userAgent;

    @Override
    public CompletableFuture<String> extractTitleAsync(String url) {
        if (!titleExtractionEnabled) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> extractTitle(url), asyncExecutor)
                .exceptionally(throwable -> {
                    log.warn("Failed to extract title from URL: {} - Error: {}", url, throwable.getMessage());
                    return null;
                });
    }

    @Override
    public String extractTitle(String url) {
        if (!titleExtractionEnabled) {
            return null;
        }

        try {
            log.debug("Extracting title from URL: {}", url);

            // Set up headers for HTML content
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, userAgent);
            headers.set(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE + ", " + MediaType.APPLICATION_XHTML_XML_VALUE);
            headers.set(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate");
            headers.set(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9");

            // Make HTTP request
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String htmlContent = response.getBody();

                // Check if response is actually HTML
                String contentType = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
                if (contentType != null && !contentType.toLowerCase().contains("text/html") &&
                        !contentType.toLowerCase().contains("application/xhtml+xml")) {
                    log.debug("Response is not HTML content type: {}", contentType);
                    return null;
                }

                // Check content length to avoid processing extremely large responses
                if (htmlContent.length() > maxContentLength) {
                    log.debug("HTML content too large ({} chars), truncating for title extraction",
                            htmlContent.length());
                    htmlContent = htmlContent.substring(0, maxContentLength);
                }

                // Check if content actually contains HTML
                if (!htmlContent.toLowerCase().contains("<html") && !htmlContent.toLowerCase().contains("<title")) {
                    log.debug("Content does not appear to be HTML");
                    return null;
                }

                String title = extractTitleFromHtml(htmlContent);
                if (title != null && !title.trim().isEmpty()) {
                    // Clean and truncate title
                    title = cleanTitle(title);
                    log.debug("Successfully extracted title: '{}' from URL: {}", title, url);
                    return title;
                }
            }

            log.debug("No title found or empty response from URL: {}", url);
            return null;

        } catch (ResourceAccessException e) {
            log.debug("Network error while extracting title from URL: {} - {}", url, e.getMessage());
            return null;
        } catch (Exception e) {
            log.debug("Unexpected error while extracting title from URL: {} - {}", url, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isTitleExtractionEnabled() {
        return titleExtractionEnabled;
    }

    /**
     * Extracts title from HTML content using proper HTML parsing.
     * Looks for title in order of preference:
     * 1. <title> tag in <head> section
     * 2. Open Graph title (og:title) - if enabled
     * 3. Meta description as fallback - if enabled
     * 4. Any title tag anywhere in the document
     * 
     * @param htmlContent The HTML content to parse
     * @return The extracted title, or null if not found
     */
    private String extractTitleFromHtml(String htmlContent) {
        try {
            Document doc = Jsoup.parse(htmlContent);

            // First priority: <title> tag in <head> section
            Element titleElement = doc.select("head > title").first();
            if (titleElement != null && !titleElement.text().trim().isEmpty()) {
                log.debug("Found title tag: '{}'", titleElement.text().trim());
                return titleElement.text().trim();
            }

            // Second priority: Open Graph title (if enabled)
            if (useOpenGraph) {
                Element ogTitleElement = doc.select("meta[property=og:title]").first();
                if (ogTitleElement != null && !ogTitleElement.attr("content").trim().isEmpty()) {
                    log.debug("Found Open Graph title: '{}'", ogTitleElement.attr("content").trim());
                    return ogTitleElement.attr("content").trim();
                }
            }

            // Third priority: Meta description as fallback (if enabled)
            if (fallbackToDescription) {
                Element metaDescElement = doc.select("meta[name=description]").first();
                if (metaDescElement != null && !metaDescElement.attr("content").trim().isEmpty()) {
                    String description = metaDescElement.attr("content").trim();
                    // Truncate description to make it suitable as a title
                    if (description.length() > 50) {
                        description = description.substring(0, 47) + "...";
                    }
                    log.debug("Using meta description as title: '{}'", description);
                    return description;
                }
            }

            // Fourth priority: Any title tag anywhere in the document
            titleElement = doc.select("title").first();
            if (titleElement != null && !titleElement.text().trim().isEmpty()) {
                log.debug("Found title tag (anywhere): '{}'", titleElement.text().trim());
                return titleElement.text().trim();
            }

            log.debug("No suitable title found in HTML content");
            return null;

        } catch (Exception e) {
            log.debug("Error parsing HTML content: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Cleans and truncates the extracted title.
     * 
     * @param title The raw title from HTML
     * @return Cleaned and truncated title
     */
    private String cleanTitle(String title) {
        if (title == null) {
            return null;
        }

        // Remove extra whitespace and newlines
        title = title.replaceAll("\\s+", " ").trim();

        // Remove common HTML entities
        title = title.replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .replaceAll("&#39;", "'");

        // Truncate to fit database field (30 characters)
        if (title.length() > 30) {
            title = title.substring(0, 27) + "...";
        }

        return title;
    }
}
