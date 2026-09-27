# Frontend plan: layout + navigation (desktop + mobile)

Date: 2026-01-26

## Existing components you must reuse
- `Header` renders `MainNav` + `SideNav` for mobile: `frontend/src/components/common/Header.tsx`.
- Navigation config is centralized in `frontend/src/config/navigation.ts`.
- Dashboard route: `frontend/src/routes/dashboard.tsx`.
- Settings route and tabs already exist: `frontend/src/routes/settings.tsx`.

## Required changes

### C1. Navigation items
Update `frontend/src/config/navigation.ts`:
- `mainNavLinks` should include:
  - Dashboard (`/dashboard`)
  - All Links (`/links`)
  - Analytics (`/analytics`)
  - Settings (`/settings`)
- Remove “Home” from mainNavLinks when user is logged in or keep it if required; for this task keep main nav focused on the four menu items listed.
- Ensure `requiresAuth: true` for all four items.

### C2. New routes
Create new route files under `frontend/src/routes`:
- `links.tsx` for All Links page.
- `analytics.tsx` for Analytics page (start minimal: header + placeholder or reused cards).

Use `createFileRoute` like existing routes and enforce auth (same pattern as `dashboard.tsx`).

### C3. Layout adjustments
- Keep `Header` as global shell. Ensure the new routes show in both desktop and mobile navigation:
  - Desktop: `MainNav` uses `mainNavLinks`.
  - Mobile: `SideNav` uses `MainNav` (mobile) and `UserNav`.
- Add page-level headers that are consistent across Dashboard, All Links, Analytics:
  - Page title
  - Optional description
  - “Add New Link” button aligned right (desktop) or as a floating action button (mobile).

### C4. Add New Link placement
Use one of these concrete options (pick one, keep consistent):
- **Option A (simplest):** render `URLShortener` at top of All Links and Dashboard, keep inline form.
- **Option B (cleaner):** create a `Dialog` or `Sheet` with `URLShortener` inside; trigger from a button in page header.

Recommendation for consistency: **Option B** on All Links, keep inline `URLShortener` only on Dashboard.

## Acceptance criteria
- Desktop nav shows 4 items: Dashboard, All Links, Analytics, Settings.
- Mobile nav shows same items (through `SideNav`).
- All Links and Analytics routes are protected and load without crashes.
- Add New Link action is easy to find on both desktop and mobile.

