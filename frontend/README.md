# TinyLS Frontend

The frontend application for TinyLS, built with React, TanStack Router, and TanStack Query.

## Features

- 🔗 URL shortening with custom aliases
<!-- - 📊 Real-time analytics dashboard -->
- 🔍 QR code generation
- 🌙 Dark mode support
- 📱 Responsive design
- 🎨 Modern UI with Tailwind CSS
- 🧪 Comprehensive test suite
- 📈 Performance monitoring
- 🔒 Secure authentication with OAuth2
- 🔄 Real-time updates
- 🌐 Internationalization support

## Prerequisites

- Node 22+
- npm or yarn
- Docker (optional)

## Development Setup

### 1. Install Dependencies

```bash
npm install
# or
yarn install
```

### 2. Environment Variables

Create a `.env` file:
```env
VITE_API_URL=http://localhost:8000
NODE_ENV=development
```

### 3. Generate API Client

```bash
cd ..
./scripts/generate_client.sh
```

### 4. Start Development Server

```bash
npm run dev
# or
yarn dev
```

## Project Structure

```
frontend/
├── src/
│   ├── assets/           # Static assets
│   ├── components/       # React components
│   ├── hooks/           # Custom React hooks
│   ├── layouts/         # Layout components
│   ├── lib/             # Utility libraries
│   ├── routes/          # TanStack Router routes
│   ├── store/           # Zustand state management
│   ├── utils/           # Utility functions
│   └── main.tsx        # Application entry point
├── public/             # Public assets
├── tests/              # Test files
├── .env               # Environment variables
└── vite.config.ts     # Vite configuration
```

## Available Scripts

- `npm run dev`: Start development server
- `npm run build`: Build for production
- `npm run preview`: Preview production build
- `npm run test`: Run tests
- `npm run lint`: Run linter
- `npm run format`: Format code
- `npm run type-check`: Check TypeScript types
- `npm run test:e2e`: Run end-to-end tests
- `npm run test:coverage`: Generate test coverage report

## Components

### Core Components

- `UrlShortener.tsx`: URL shortening form
- `UrlList.tsx`: List of shortened URLs

### Layout Components

- `index.tsx`: Default page layout
- `login.tsx`: Authentication pages layout
- `dashboard.tsx`: Dashboard pages layout

## Routing

The application uses TanStack Router for routing. Routes are defined in `src/routes/`:

```typescript
import { createRoute } from '@tanstack/react-router'

export const rootRoute = createRoute({
  getParentRoute: () => null,
  path: '/',
})

export const dashboardRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: 'dashboard',
})
```

## Data Fetching

The application uses TanStack Query for data fetching:

```typescript
import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api'

export function useUrls() {
  return useQuery({
    queryKey: ['urls'],
    queryFn: () => api.urls.list(),
  })
}
```

## API Integration

API calls are made using the generated API client from hey-api:

```typescript
import { api } from '@/lib/api'

// Create URL
const response = await api.urls.create({
  originalUrl: 'https://example.com',
  alias: 'example'
})

// Get URLs
const urls = await api.urls.list()
```

## Styling

The application uses Tailwind CSS for styling. Key features:

- Responsive design
- Dark mode support
- Custom components
- Utility-first approach
- CSS-in-JS with styled-components
- CSS Modules for component-specific styles

## Testing

### Unit Tests

```bash
npm run test:unit
```

### Component Tests

```bash
npm run test:component
```

### E2E Tests

```bash
npm run test:e2e
```

### Test Coverage

```bash
npm run test:coverage
```

## Performance Optimization

### Code Splitting

- Route-based code splitting
- Component lazy loading
- Dynamic imports
- Preloading critical resources

### Asset Optimization

- Image optimization
- Font loading optimization
- CSS/JS minification
- Tree shaking
- Module concatenation

### Caching

- TanStack Query caching
- Service Worker caching
- Static asset caching
- Browser cache headers
- CDN caching

## Security

### Best Practices

- CSRF protection
- XSS prevention
- Content Security Policy
- Secure cookie handling
- Input sanitization
- Output encoding

### Authentication

- JWT token management
- Refresh token rotation
- Secure storage
- OAuth2 integration
- Session management

## Error Handling

### Global Error Handler

```typescript
import { ErrorBoundary } from 'react-error-boundary'

function ErrorFallback({ error }) {
  return (
    <div role="alert">
      <p>Something went wrong:</p>
      <pre>{error.message}</pre>
    </div>
  )
}

<ErrorBoundary FallbackComponent={ErrorFallback}>
  <App />
</ErrorBoundary>
```

### API Error Handling

```typescript
import { useQuery } from '@tanstack/react-query'

const { data, error } = useQuery({
  queryKey: ['urls'],
  queryFn: () => api.urls.list(),
  retry: 3,
  onError: (error) => {
    // Handle error
  }
})
```

## Monitoring

### Performance Monitoring

- React DevTools
- Browser Performance API
- Custom metrics
- Real User Monitoring (RUM)
- Synthetic monitoring

### Error Tracking

- Error boundary components
- Error logging service
- User feedback collection
- Crash reporting
- Performance monitoring

## Build and Deployment

### Production Build

```bash
npm run build
```

### Docker Build

```bash
docker build -t tinyls-frontend .
```

### Docker Run

```bash
docker run -p 80:80 tinyls-frontend
```

## CI/CD Pipeline

The frontend uses GitHub Actions for continuous integration and deployment:

### Workflows

- **Dependency Updates**: Weekly automated dependency checks
- **Code Formatting**: Automated code formatting with Prettier
- **Testing**: Automated testing for unit, component, and E2E tests
- **Code Review**: Automated code quality checks with SonarQube and CodeQL
- **Staging Deployment**: Automated deployment to staging environment
- **Production Deployment**: Automated deployment to production environment

### Quality Gates

- Code coverage requirements
- Security scan results
- Performance benchmarks
- Accessibility standards
- Bundle size limits

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details.
