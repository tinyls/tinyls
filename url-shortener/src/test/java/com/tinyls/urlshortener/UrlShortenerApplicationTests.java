package com.tinyls.urlshortener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import com.tinyls.urlshortener.dto.url.UrlDTO;
import com.tinyls.urlshortener.model.UrlStatus;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UrlShortenerApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void fullUrlStatusIntegrationTest() {
        // 1. Create a URL (anonymous)
        UrlDTO createRequest = UrlDTO.builder().originalUrl("https://integration-test.com").build();
        ResponseEntity<UrlDTO> createResp = restTemplate.postForEntity(
                "/api/urls/", createRequest, UrlDTO.class);
        assertEquals(HttpStatus.CREATED, createResp.getStatusCode());
        UrlDTO created = createResp.getBody();
        assertNotNull(created);
        String shortCode = created.getShortCode();

        // 2. Redirect should work (ACTIVE)
        ResponseEntity<Void> redirectResp = restTemplate.getForEntity(
                "/api/urls/r/" + shortCode, Void.class);
        assertEquals(HttpStatus.FOUND, redirectResp.getStatusCode());
        assertEquals("https://integration-test.com", redirectResp.getHeaders().getLocation().toString());

        // 3. Toggle to INACTIVE
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String statusBody = "{\"status\":\"INACTIVE\"}";
        HttpEntity<String> patchEntity = new HttpEntity<>(statusBody, headers);
        ResponseEntity<UrlDTO> patchResp = restTemplate.exchange(
                "/api/urls/" + shortCode + "/status", HttpMethod.PATCH, patchEntity, UrlDTO.class);
        assertEquals(HttpStatus.OK, patchResp.getStatusCode());
        assertEquals(UrlStatus.INACTIVE, patchResp.getBody().getStatus());

        // 4. Redirect should now fail (404)
        ResponseEntity<Void> redirectFail = restTemplate.getForEntity(
                "/api/urls/r/" + shortCode, Void.class);
        assertEquals(HttpStatus.NOT_FOUND, redirectFail.getStatusCode());

        // 5. Toggle back to ACTIVE
        String statusBodyActive = "{\"status\":\"ACTIVE\"}";
        HttpEntity<String> patchEntityActive = new HttpEntity<>(statusBodyActive, headers);
        ResponseEntity<UrlDTO> patchRespActive = restTemplate.exchange(
                "/api/urls/" + shortCode + "/status", HttpMethod.PATCH, patchEntityActive, UrlDTO.class);
        assertEquals(HttpStatus.OK, patchRespActive.getStatusCode());
        assertEquals(UrlStatus.ACTIVE, patchRespActive.getBody().getStatus());

        // 6. Redirect should work again
        ResponseEntity<Void> redirectAgain = restTemplate.getForEntity(
                "/api/urls/r/" + shortCode, Void.class);
        assertEquals(HttpStatus.FOUND, redirectAgain.getStatusCode());
        assertEquals("https://integration-test.com", redirectAgain.getHeaders().getLocation().toString());
    }
}
