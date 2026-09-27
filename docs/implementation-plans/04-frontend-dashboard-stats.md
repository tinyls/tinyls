# Frontend plan: dashboard stats UI

Date: 2026-01-26

## Existing components to reuse
- `URLShortener` at `frontend/src/components/url-shortener/URLShortener` (currently used in dashboard).
- `dashboard.tsx` route at `frontend/src/routes/dashboard.tsx`.
- shadcn `Card` at `frontend/src/components/ui/card.tsx`.

## Required changes

### D1. API client and hooks
- Once backend adds `/api/analytics/summary`, update OpenAPI client:
  - Update the OpenAPI spec used by the frontend (likely `frontend/openapi.json`).
  - Run `npm run generate-api` to regenerate `frontend/src/api`.
- Create a hook `frontend/src/hooks/useAnalytics.ts`:
  - `useDashboardSummaryQuery()` using the generated client.
  - Use a stable query key like `analytics-summary`.

### D2. UI components
- Create `frontend/src/components/dashboard/StatsCards.tsx`:
  - Accept summary data or fetch internally via hook.
  - Render 4 cards:
    - Total Links
    - Active Links
    - Inactive Links
    - Clicks last 24h
    - Clicks last 7d
  - Use responsive grid (1 col on mobile, 2 on small, 4 on desktop).
  - Use subtle loading skeletons while query is loading.

### D3. Dashboard layout
- Update `frontend/src/routes/dashboard.tsx`:
  - Add stats section near top.
  - Keep `URLShortener` form, but reduce width and move to side or below stats.
  - Remove `URLHistory` from dashboard if it’s moved to All Links.

## Acceptance criteria
- Dashboard fetches and displays the summary from backend.
- UI remains responsive and readable on mobile.
- No regression in existing URL shortener flow.

