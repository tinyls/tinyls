# Frontend plan: All Links table + filters + mobile layout

Date: 2026-01-26

## Existing components to reuse
- `URLHistory` at `frontend/src/components/url-shortener/URLHistory.tsx`.
- Search bar `frontend/src/components/ui/search` (already used in URLHistory).
- shadcn table, dropdown, badge, button components.

## Required changes

### E1. Route and page shell
- Move `URLHistory` off the Dashboard and render it in the new All Links page:
  - `frontend/src/routes/links.tsx`.
- Page header:
  - Title: “All Links”
  - “Add New Link” button (opens dialog with `URLShortener` or inline form).

### E2. URLHistory data model updates
- Update the `UrlData` mapping to include:
  - `title`
  - `status`
  - `createdAt`, `updatedAt`
  - `clicks`
- Fallback rules:
  - If `title` is empty, display `shortCode` or truncated `originalUrl`.

### E3. Table columns and actions
- Replace columns with:
  - Title
  - Clicks
  - Created / Updated
  - Status (Badge)
  - Actions (three-dot menu)
- Actions menu (shadcn `DropdownMenu`):
  - Copy short URL
  - Open short URL
  - Toggle status (active/inactive) using existing PATCH endpoint
  - Delete

### E4. Filters + sorting
- Add filter controls above the table:
  - Status filter (All / Active / Inactive)
  - Sort dropdown: Title, Created, Updated, Clicks
  - Sort direction toggle (asc/desc)
- Update `useUrlHistoryQuery` to accept query params and pass them to `useGetUrlsByUser` once backend supports params.
- Keep client-side search bar, but if backend supports `search`, pass it as query param instead of filtering in memory.

### E5. Mobile layout
- Switch to stacked cards on `sm` and below:
  - Each card shows Title, Clicks, Status badge, Created/Updated.
  - Actions menu aligned to top-right of card.
- Filter controls stack vertically on small screens.

## Acceptance criteria
- Table shows the new columns and menu.
- Sorting and filtering work with backend params.
- Mobile cards are readable and thumb-friendly.

