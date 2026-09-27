# Backend audit (Spring Boot) — existing vs missing

Date: 2026-01-26

## Existing and stable (already implemented)

- URL entity with status, title, created/updated timestamps, and click counter:
  - `url-shortener/src/main/java/com/tinyls/urlshortener/model/Url.java`
  - Fields: `title`, `status`, `createdAt`, `updatedAt`, `clicks`, `user`.
- URL list for authenticated user (no pagination/filtering):
  - `UrlController.getUrlsByUser()` at `url-shortener/src/main/java/com/tinyls/urlshortener/controller/UrlController.java`
  - Service method `UrlService.getUrlsByUser()` at `url-shortener/src/main/java/com/tinyls/urlshortener/service/UrlService.java`
  - Implementation `UrlServiceImpl.getUrlsByUser()` at `url-shortener/src/main/java/com/tinyls/urlshortener/service/impl/UrlServiceImpl.java`
- URL status updates (ACTIVE/INACTIVE):
  - `PATCH /api/urls/id/{id}/status` in `UrlController`.
  - Implementation `UrlServiceImpl.updateUrlStatusById()`.
- Clicks are incremented on redirect:
  - `UrlController.redirectToUrl()` calls `UrlService.getAndIncrementClicks()`.
  - `UrlServiceImpl.getAndIncrementClicks()` increments `Url.clicks` and updates caches.
- DTO includes all required fields already for table display:
  - `url-shortener/src/main/java/com/tinyls/urlshortener/dto/url/UrlDTO.java` includes `title`, `status`, `createdAt`, `updatedAt`, `clicks`, `shortCode`.
- Feature flags exist for analytics/detailed click tracking (not wired to actual analytics):
  - `url-shortener/src/main/java/com/tinyls/urlshortener/config/FeatureFlags.java`
  - `FeatureFlagService` and `FeatureFlagController` exist.

## Missing or not implemented (required by new requirements)

- No time-window click analytics:
  - There is no click-event table (only aggregate `Url.clicks`).
  - Without event timestamps, clicks in last 24 hours / 7 days cannot be computed.
- No analytics summary endpoint:
  - There is no `/api/analytics/summary` or equivalent.
- No server-side filtering/sorting/pagination in URLs list:
  - `GET /api/urls/` only returns `List<UrlDTO>` for a user with no query params.
  - The frontend currently filters client-side using search bar only.

## Backend impact summary

- **Stable & reusable**: URL entity, clicks counter, status update endpoint, existing list endpoint, DTO fields.
- **Needs implementation**: click-event persistence, analytics summary service/controller, URL list filtering/sorting/pagination support.

