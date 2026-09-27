# Implementation Plan: Link Statistics + Dashboard Navigation + Table UX

Date: 2026-01-26

## Goals
- Provide logged-in users a dashboard with key link statistics: total links, active vs inactive counts, clicks last 24 hours, clicks last 7 days.
- Update dashboard layout to include navigation menus: Dashboard, All Links, Analytics, Settings.
- Enhance the links table to show: title, click count, created/updated date, status, and an actions (three-dot) menu.
- Add filtering and sorting on the links table; keep the existing search bar.
- Ensure UX is excellent on both desktop and mobile.
- Use TailwindCSS + shadcn/ui components.
- Implement missing backend analytics in Spring Boot.

## Current Context (from repo scan)
- Backend Spring Boot code under `url-shortener/src/main/java/com/tinyls/urlshortener`.
- URL entity is `Url` with fields: `clicks`, `title`, `status`, `createdAt`, `updatedAt`, `user`.
- No click-event table exists yet; clicks are incremented on the `Url` row.
- Frontend React app under `frontend/src` with `dashboard` route and `URLHistory` table.
- `URLHistory` currently shows original URL, short URL, created, clicks, and action icons.

## High-Level Approach
1) **Backend**: Introduce click-event tracking so time-window analytics are possible. Add analytics query endpoints for dashboard summary. Extend list endpoint to support filtering/sorting and return data required by the new table.
2) **Frontend**: Build a dashboard layout with left nav (desktop) and a responsive nav pattern (mobile). Add stats cards. Update links table and add filtering/sorting controls. Provide a friendly mobile card layout for links.

---

## Component A: Backend Analytics Data Model

**Objective**: Enable time-window click counts per user (24h, 7d) while retaining total clicks stored on `Url`.

### A1. Data Model & Migration
- **New entity**: `UrlClick` (or `LinkClick`).
- **Fields**:
  - `id` (PK)
  - `url` (FK -> Url)
  - `clickedAt` (timestamp)
  - Optional metadata: `referrer`, `userAgent`, `ipHash`
- **Indexes**:
  - `(url_id, clicked_at)` for time-range queries
  - `(clicked_at)` optional for aggregate queries

**Files**:
- `url-shortener/src/main/java/com/tinyls/urlshortener/model/UrlClick.java`
- `url-shortener/src/main/java/com/tinyls/urlshortener/repository/UrlClickRepository.java`
- Flyway/Liquibase migration (if used) or JPA auto DDL config

**Acceptance Criteria**:
- New table exists and can store per-click events.
- Indexes are created to keep time-window queries fast.

### A2. Click Tracking Integration
- Update the redirect flow (likely in `UrlServiceImpl.getAndIncrementClicks`) to:
  - Continue incrementing `Url.clicks` (existing behavior).
  - Create a `UrlClick` record when a click occurs.
- Feature flag handling: If `detailedClickTracking` exists, gate this logic accordingly.

**Files**:
- `url-shortener/src/main/java/com/tinyls/urlshortener/service/impl/UrlServiceImpl.java`
- `url-shortener/src/main/java/com/tinyls/urlshortener/config/FeatureFlags.java` (if needed)

**Acceptance Criteria**:
- On each click, a `UrlClick` row is inserted (unless feature flag disabled).
- No regression in redirect performance.

---

## Component B: Backend Analytics Queries + API

**Objective**: Provide a summary endpoint and enhance the links list endpoint with filtering/sorting.

### B1. Repository Queries
- Total links for user: `count(*) where user_id = ?`
- Active/inactive counts for user: `count where status = ACTIVE/INACTIVE`
- Clicks last 24h: count of `UrlClick` joined to `Url` by user, `clickedAt >= now - 24h`
- Clicks last 7d: count of `UrlClick` joined to `Url` by user, `clickedAt >= now - 7d`

**Files**:
- `url-shortener/src/main/java/com/tinyls/urlshortener/repository/UrlRepository.java`
- `url-shortener/src/main/java/com/tinyls/urlshortener/repository/UrlClickRepository.java`

**Acceptance Criteria**:
- Queries return accurate counts and are scoped to the authenticated user.

### B2. Service Layer
- Create `AnalyticsService`:
  - `getDashboardSummary(userId)`
- Create DTO: `DashboardSummaryDTO`
  - `totalLinks`, `activeLinks`, `inactiveLinks`, `clicksLast24h`, `clicksLast7d`

**Files**:
- `url-shortener/src/main/java/com/tinyls/urlshortener/service/AnalyticsService.java`
- `url-shortener/src/main/java/com/tinyls/urlshortener/service/impl/AnalyticsServiceImpl.java`
- `url-shortener/src/main/java/com/tinyls/urlshortener/dto/analytics/DashboardSummaryDTO.java`

**Acceptance Criteria**:
- Service returns correct values with minimal database calls.

### B3. REST Endpoints
- `GET /api/analytics/summary`
  - Response: `DashboardSummaryDTO`
- Enhance existing `GET /api/urls` (or current list endpoint):
  - Query params: `search`, `status`, `sortBy`, `sortDir`, `page`, `pageSize`
  - Response: include `title`, `status`, `createdAt`, `updatedAt`, `clicks`, and optionally `shortCode`.

**Files**:
- `url-shortener/src/main/java/com/tinyls/urlshortener/controller/AnalyticsController.java`
- `url-shortener/src/main/java/com/tinyls/urlshortener/controller/UrlController.java`
- DTO updates in `url-shortener/src/main/java/com/tinyls/urlshortener/dto/url/UrlDTO.java`

**Acceptance Criteria**:
- Summary endpoint is secured and uses authenticated user.
- Link list endpoint supports sorting/filtering without breaking search.

---

## Component C: Frontend Navigation + Layout

**Objective**: Introduce top-level navigation and adjust dashboard layout to support new sections.

### C1. Routing + Layout Shell
- Add a shared layout for authenticated routes with navigation:
  - Desktop: left sidebar navigation.
  - Mobile: bottom tab bar or top app bar + drawer.
- Menu items: Dashboard, All Links, Analytics, Settings.
- Add “Add New Link” button in header (desktop) and as floating action button (mobile).

**Files**:
- `frontend/src/routes` (new layout route file or wrapper)
- Potential new component: `frontend/src/components/layout/AppShell.tsx`

**Acceptance Criteria**:
- Navigation is visible and usable on desktop and mobile.
- Add Link button is accessible on all relevant pages.

---

## Component D: Dashboard Stats UI

**Objective**: Present summary stats cards on dashboard.

### D1. Stats Cards
- Use shadcn/ui `Card` components with Tailwind layout.
- Cards:
  - Total Links
  - Active / Inactive Links (can be 2 cards or one split card)
  - Clicks (Last 24h)
  - Clicks (Last 7d)

**Files**:
- `frontend/src/routes/dashboard.tsx`
- `frontend/src/components/dashboard/StatsCards.tsx` (new)

**Acceptance Criteria**:
- Cards are responsive and display values from `/api/analytics/summary`.
- Skeleton or loading state while fetching.

---

## Component E: Links Table + Filtering/Sorting

**Objective**: Upgrade the links list UI and functionality.

### E1. Table Columns + Data
- Columns: Title, Click Count, Created/Updated, Status (badge), Actions (three-dot menu).
- Add a three-dot dropdown menu for link actions (edit, copy, open, disable, delete).
- Include `title` fallback to short code or original URL if empty.

**Files**:
- `frontend/src/components/url-shortener/URLHistory.tsx`
- `frontend/src/components/ui/dropdown-menu.tsx` usage

**Acceptance Criteria**:
- Table shows required columns and three-dot menu.

### E2. Filtering + Sorting UI
- Search bar remains.
- Add filter control (status: all/active/inactive).
- Add sort control (title, createdAt, updatedAt, clicks).
- Use query params to request sorted/filtered data from backend.

**Files**:
- `frontend/src/components/url-shortener/URLHistory.tsx`
- `frontend/src/hooks/useUrlShortener.ts` (if query params added)

**Acceptance Criteria**:
- Filters and sorting affect results and are debounced if needed.

### E3. Mobile UX
- Replace table with stacked cards on small screens.
- Each card shows title, clicks, status badge, created/updated, actions menu.
- Filters and search collapse into a single row or a filter drawer.

**Acceptance Criteria**:
- Mobile experience is readable, touch-friendly, and efficient.

---

## Component F: Analytics Page (Optional Extension)

**Objective**: A dedicated Analytics page in the nav (even if minimal initially).

### F1. Analytics Route
- Create route `/analytics` with a basic placeholder or future charts.
- Reuse summary cards or show “coming soon” while backend matures.

**Files**:
- `frontend/src/routes/analytics.tsx`

**Acceptance Criteria**:
- Nav entry works and page is accessible on desktop/mobile.

---

## Testing & QA Checklist

### Backend
- Unit tests for analytics service and repositories.
- Integration test for summary endpoint.
- Validate query performance with indexes.

### Frontend
- Visual QA on desktop + mobile breakpoints.
- Sorting and filtering correctness.
- Empty states and loading states.
- Accessibility: tab focus, ARIA for menus.

---

## Deliverables Summary
- New backend analytics storage and summary endpoint.
- Enhanced URL list endpoint with filters/sorting.
- New dashboard layout with nav and stats cards.
- Updated links table with required fields + three-dot menu.
- Responsive and mobile-friendly UI for all changes.

