package com.tinyls.urlshortener.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;

/**
 * Configuration class for URL title extraction service.
 * Provides RestTemplate and async executor configuration.
 */
@Configuration
public class UrlTitleExtractionConfig {

    @Value("${url.title.extraction.timeout:5000}")
    private int timeoutMs;

    @Value("${url.title.extraction.max-threads:5}")
    private int maxThreads;

    /**
     * Creates a RestTemplate configured for URL title extraction.
     * Sets appropriate timeouts and user agent.
     * 
     * @return Configured RestTemplate instance
     */
    @Bean
    public RestTemplate urlTitleExtractionRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);

        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }

    /**
     * Creates an async executor for URL title extraction tasks.
     * Uses a dedicated thread pool to avoid blocking the main application threads.
     * 
     * @return Configured Executor instance
     */
    @Bean("urlTitleExtractionExecutor")
    public Executor urlTitleExtractionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(maxThreads);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("url-title-extractor-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
