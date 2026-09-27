# Testing plan (backend + frontend)

Date: 2026-01-26

## Backend tests (Spring Boot)

### Unit tests
- Add `AnalyticsServiceImplTest` in `url-shortener/src/test/java/com/tinyls/urlshortener/service`:
  - Seed 2 users, 3 URLs, 5 click events.
  - Assert `totalLinks`, `active/inactive`, `clicksLast24h`, `clicksLast7d`.
- Add `UrlClickRepositoryTest` if using JPA slice tests to validate time-window queries.

### Integration tests
- `AnalyticsControllerTest` in `url-shortener/src/test/java/com/tinyls/urlshortener/controller`:
  - Authenticate a user and call `/api/analytics/summary`.
  - Assert JSON shape and values.
- Extend `UrlControllerTest` to cover:
  - `GET /api/urls/` with `status`, `search`, `sortBy`, `sortDir` params.

### Data setup
- Because `application-test.properties` currently disables Flyway, ensure test setup creates `url_clicks` table.
  - Option A: enable Flyway in tests.
  - Option B: create table manually in test setup.

## Frontend tests (React)

### Unit/Component tests (Vitest)
- Add tests for `StatsCards` to render values and loading state.
- Add tests for `URLHistory` (or new `LinksTable`) to:
  - Render table columns.
  - Open actions dropdown.
  - Apply filter/sort UI.

### Manual QA checklist
- Desktop:
  - Navigation shows Dashboard/All Links/Analytics/Settings.
  - Dashboard stats load and display accurate counts.
  - All Links table shows title/clicks/created-updated/status.
  - Actions menu works (copy/open/toggle/delete).
- Mobile:
  - Navigation accessible and usable.
  - All Links uses card layout with actions menu.
  - Filters and search are easy to access.

