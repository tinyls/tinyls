# Backend implementation plan: analytics + filtering

Date: 2026-01-26

This plan assumes the existing `Url` model, clicks counter, status updates, and `/api/urls/` list endpoint remain in place. It adds time-window analytics and server-side list filtering/sorting.

## B0. Decisions locked by current code
- **Clicks are already tracked as an aggregate on `Url.clicks`** and used for existing UI (`UrlDTO.clicks`).
- **Redirect + click increment is already implemented** in `UrlServiceImpl.getAndIncrementClicks()`.
- **Status enum** already exists: `UrlStatus`.
- **DTO already exposes all fields needed for UI** (`UrlDTO`).

These are stable; do not remove or replace them. Extend only.

---

## B1. Add click-event persistence (required for 24h/7d)

### B1.1 Schema migration (Flyway)
- Add new migration in `url-shortener/src/main/resources/db/migration`:
  - `V8__create_url_clicks_table.sql`
- Table schema (Postgres-friendly):
  - `id BIGSERIAL PRIMARY KEY`
  - `url_id BIGINT NOT NULL REFERENCES urls(id) ON DELETE CASCADE`
  - `clicked_at TIMESTAMP NOT NULL DEFAULT NOW()`
  - optional: `referrer TEXT`, `user_agent TEXT`, `ip_hash TEXT`
- Indexes:
  - `CREATE INDEX idx_url_clicks_url_id_clicked_at ON url_clicks (url_id, clicked_at);`
  - `CREATE INDEX idx_url_clicks_clicked_at ON url_clicks (clicked_at);`

### B1.2 Entity + repository
- Create entity: `url-shortener/src/main/java/com/tinyls/urlshortener/model/UrlClick.java`
- Create repository: `url-shortener/src/main/java/com/tinyls/urlshortener/repository/UrlClickRepository.java`
- Keep entity lightweight; only fields needed for time-window counts.

### B1.3 Insert click events on redirect
- Update `UrlServiceImpl.getAndIncrementClicks()`:
  - After confirming ACTIVE URL, insert `UrlClick` record.
  - Keep existing `Url.clicks` increment intact.
  - Optional: gate behind `FeatureFlagService.isDetailedClickTrackingEnabled()` to avoid unnecessary writes if disabled.

**Acceptance criteria**:
- Each redirect inserts a click-event row.
- Existing click count behavior remains unchanged.

---

## B2. Analytics queries + service

### B2.1 Repository queries
Add query methods to `UrlClickRepository` and/or `UrlRepository`:
- **Clicks last 24h**: count of `UrlClick` joined by `url.user.id = :userId` and `clickedAt >= now - 24h`.
- **Clicks last 7d**: same with `>= now - 7 days`.
- **Total links**, **active**, **inactive**:
  - Add `countByUserId(UUID userId)` and `countByUserIdAndStatus(UUID userId, UrlStatus status)` in `UrlRepository`.

### B2.2 Service + DTO
- Create `DashboardSummaryDTO` at `url-shortener/src/main/java/com/tinyls/urlshortener/dto/analytics/DashboardSummaryDTO.java`.
- Create `AnalyticsService` and `AnalyticsServiceImpl`:
  - `getDashboardSummary(UUID userId)` returns the DTO.

### B2.3 Controller endpoint
- Add `AnalyticsController` at `url-shortener/src/main/java/com/tinyls/urlshortener/controller/AnalyticsController.java`:
  - `GET /api/analytics/summary`
  - Requires authentication; userId derived from `UserDetailsAdapter`.

**Acceptance criteria**:
- Endpoint returns exact counts for the authenticated user.
- Works even if user has 0 links (all numbers = 0).

---

## B3. URLs list filtering/sorting (server-side)

### B3.1 API contract update
Enhance `GET /api/urls/` with optional params:
- `search` (string) — match `originalUrl`, `shortCode`, and `title`.
- `status` (ACTIVE/INACTIVE)
- `sortBy` (createdAt | updatedAt | clicks | title)
- `sortDir` (asc | desc)
- `page` (default 0)
- `pageSize` (default 20)

### B3.2 Repository support
- Add `findByUserId` overload using `Pageable`.
- Add custom query for search across multiple fields.

### B3.3 Service changes
- Update `UrlService.getUrlsByUser()` signature or add new method to accept filters/sort/pagination.
- Map results to `UrlDTO` list + include metadata (total pages, total count) if needed by UI.

**Acceptance criteria**:
- Filters/sorts behave deterministically and are tested.
- Old behavior preserved when no query params are present.

---

## B4. Backend testing plan

### Unit tests
- `UrlClickRepository` query methods for 24h/7d windows.
- `AnalyticsServiceImpl` returns correct counts for seed data.
- Update `UrlServiceImplTest` to ensure click-event insert occurs (when enabled).

### Integration tests
- `AnalyticsController` returns expected values for a seeded user.
- `UrlController.getUrlsByUser` with query params returns correct order + filters.

**Note**: `application-test.properties` has `spring.flyway.enabled=false`. If using Flyway migration in tests, update test config accordingly or add explicit schema creation for `url_clicks` in test setup.

