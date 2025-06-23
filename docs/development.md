# Development Guide

This guide will help you set up the tinyls project for local development and contribute to the project.

## Prerequisites

- Docker and Docker Compose
- Java 21
- Node.js 18+
- Maven
- Git
- IDE (IntelliJ IDEA, VS Code, or Eclipse)
- PostgreSQL (optional, if not using Docker)

## Local Development Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/tinyls.git
cd tinyls
```

### 2. Set Up Environment Variables

1. Copy the example environment files:

   ```bash
   cp url-shortener/.env.example url-shortener/.env
   cp frontend/.env.example frontend/.env
   ```

2. Update the environment variables with your configuration (see [Environment Variables](environment-variables.md))

### 3. Start Development Environment

#### Using Docker (Recommended)

```bash
# Start all services
docker compose watch

# View logs
docker compose logs -f
```

#### Manual Setup

1. **Start PostgreSQL**

   ```bash
   docker run -d \
     --name tinyls-postgres \
     -e POSTGRES_DB=tinyls \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     postgres:15
   ```

2. **Start Backend**

   ```bash
   cd url-shortener
   ./mvnw spring-boot:run
   ```

3. **Start Frontend**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

### 4. Access the Applications

- Frontend: http://localhost:5173
- Backend API: http://localhost:8000
- Swagger UI: http://localhost:8000/swagger-ui.html
- Adminer (Database): http://localhost:8080

## Project Structure

```
tinyls/
├── frontend/           # React frontend application
│   ├── src/           # Source code
│   ├── public/        # Static assets
│   └── package.json   # Dependencies
├── url-shortener/     # Spring Boot backend
│   ├── src/          # Source code
│   └── pom.xml       # Dependencies
├── infra/            # Infrastructure
│   ├── traefik/      # Reverse proxy
│   ├── prometheus/   # Monitoring
│   └── grafana/      # Dashboards
└── docs/             # Documentation
```

## Development Workflow

### 1. Create a New Branch

```bash
git checkout -b feature/your-feature-name
```

### 2. Make Changes

- Follow the coding standards
- Write tests for new features
- Update documentation as needed

### 3. Run Tests

#### Backend Tests

```bash
cd url-shortener
./mvnw test
```

#### Frontend Tests

```bash
cd frontend
npm test
```

### 4. Commit Changes

```bash
git add .
git commit -m "add new feature"
```

### 5. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

## Coding Standards

### Backend (Java)

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Add Javadoc comments for public methods
- Write unit tests for all new features
- Use dependency injection
- Follow REST API best practices

### Frontend (React)

- Follow [Airbnb JavaScript Style Guide](https://github.com/airbnb/javascript)
- Use functional components with hooks
- Implement proper error handling
- Write unit tests using Jest
- Use TypeScript for type safety
- Follow component composition patterns

## Testing

### Backend Testing

- Unit tests with JUnit 5
- Integration tests with Spring Test
- API tests with RestAssured
- Test coverage with JaCoCo

### Frontend Testing

- Unit tests with Jest
- Component tests with React Testing Library
- E2E tests with Cypress
- Test coverage with Istanbul

## Debugging

### Backend Debugging

1. Enable debug logging in `application.properties`:

   ```properties
   logging.level.com.tinyls=DEBUG
   ```

2. Use IDE debugger:
   - Set breakpoints
   - Attach debugger to running application
   - Use remote debugging

### Frontend Debugging

1. Use React Developer Tools
2. Enable source maps in development
3. Use browser developer tools
4. Implement error boundaries

## Common Issues and Solutions

### Database Connection Issues

1. Check PostgreSQL is running
2. Verify connection properties
3. Check network connectivity
4. Ensure database exists

### Frontend Build Issues

1. Clear node_modules and reinstall
2. Check Node.js version
3. Update dependencies
4. Clear browser cache

### Docker Issues

1. Check Docker daemon is running
2. Verify port mappings
3. Check container logs
4. Rebuild containers if needed

## Performance Optimization

### Backend

- Use connection pooling
- Implement caching
- Optimize database queries
- Use async processing where appropriate

### Frontend

- Implement code splitting
- Use lazy loading
- Optimize bundle size
- Implement proper caching

## Security Best Practices

1. Never commit sensitive data
2. Use environment variables
3. Implement proper authentication
4. Follow OWASP guidelines
5. Regular security audits

## Contributing Guidelines

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Write tests
5. Update documentation
6. Create a pull request

## Getting Help

- Check existing issues
- Join our community chat
- Contact maintainers
- Read the documentation

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://reactjs.org/docs)
- [Docker Documentation](https://docs.docker.com)
- [PostgreSQL Documentation](https://www.postgresql.org/docs)
