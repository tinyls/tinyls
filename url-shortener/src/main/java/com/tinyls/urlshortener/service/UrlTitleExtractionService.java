package com.tinyls.urlshortener.service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for extracting titles from URLs.
 * Provides asynchronous title extraction to avoid blocking URL creation.
 */
public interface UrlTitleExtractionService {

    /**
     * Extracts the title from a given URL asynchronously.
     * 
     * @param url The URL to extract the title from
     * @return A CompletableFuture containing the extracted title, or null if
     *         extraction fails
     */
    CompletableFuture<String> extractTitleAsync(String url);

    /**
     * Extracts the title from a given URL synchronously.
     * Use this method only when you need to block and wait for the result.
     * 
     * @param url The URL to extract the title from
     * @return The extracted title, or null if extraction fails
     */
    String extractTitle(String url);

    /**
     * Checks if title extraction is enabled for the application.
     * 
     * @return true if title extraction is enabled, false otherwise
     */
    boolean isTitleExtractionEnabled();
}
