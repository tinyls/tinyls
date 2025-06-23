[![CI / CD - Staging](https://github.com/tinyls/tinyls/actions/workflows/deploy-staging.yml/badge.svg?branch=deployment%2Fstaging)](https://github.com/tinyls/tinyls/actions/workflows/deploy-staging.yml)

# tinyls - A lightweight URL shortener app

A modern, secure, and scalable URL shortening app built with Spring Boot, React, and PostgreSQL.

## 🌟 Features

- **URL Shortening**: Create short, memorable URLs from long ones
- **Authentication**: Secure login with Google and GitHub OAuth2
- **Analytics**: Track clicks and usage patterns
- **API**: RESTful API for programmatic access
- **Monitoring**: Prometheus metrics and Grafana dashboards
- **Security**: JWT-based authentication, HTTPS, and secure headers

## 🏗️ Architecture

The project consists of several components:

- **Frontend**: React-based web application
- **Backend**: Spring Boot REST API
- **Database**: PostgreSQL for data persistence
- **Infrastructure**: Docker, Traefik, Prometheus, and Grafana

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose
- Java 21
- Node.js 22+
- Maven
- Git

### Local Development Setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/imounish/tinyls.git
   cd tinyls
   ```

2. **Set up environment variables**

   ```bash
   cp url-shortener/.env.example url-shortener/.env
   cp frontend/.env.example frontend/.env
   ```

   Edit the `.env` files with your configuration.

3. **Start the development environment**

   ```bash
   docker compose watch
   ```

4. **Access the applications**
   - Frontend: http://localhost:5173
   - Backend API: http://localhost:8000
   - Swagger UI: http://localhost:8000/swagger-ui.html
   - Adminer (Database): http://localhost:8080

### Staging Environment

To run the staging environment locally:

```bash
docker compose -f docker-compose.yml -f docker-compose.staging.yml up -d
```

## 📚 Documentation

- [Development Guide](docs/development.md)
- [Deployment Guide](docs/deployment.md)
- [Security Policy](SECURITY.md)
- [Release Notes](release-notes.md)

## 🛠️ Project Structure

```
tinyls/
├── frontend/          # React frontend application
├── url-shortener/     # Spring Boot backend application
├── infra/             # Infrastructure configurations
│   ├── traefik/       # Traefik reverse proxy configs
│   ├── prometheus/    # Prometheus monitoring configs
│   └── grafana/       # Grafana dashboard configs
└── docs/              # Project documentation
```

## 🔧 Configuration

### Environment Variables

See [Environment Variables](docs/environment-variables.md) for detailed configuration options.

### OAuth2 Setup

1. Create OAuth2 applications in Google and GitHub
2. Configure the redirect URIs:
   - Google: `https://api.staging.tinyls.com/login/oauth2/code/google`
   - GitHub: `https://api.staging.tinyls.com/login/oauth2/code/github`
3. Add the client IDs and secrets to your environment variables

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

See [Contributing Guide](CONTRIBUTING.md) for more details.

## 🔄 CI/CD Pipeline

The project uses GitHub Actions for continuous integration and deployment:

### Workflows

- **Dependency Updates**: Weekly automated dependency checks
- **Code Formatting**: Automated code formatting with Spotless and Prettier
- **Testing**: Automated testing for backend, frontend, and E2E tests
- **Code Review**: Automated code quality checks with SonarQube, CodeQL, and Code Climate
- **Staging Deployment**: Automated deployment to staging environment
- **Production Deployment**: Automated deployment to production environment

### Environments

- **Development**: Local development environment
- **Staging**: Pre-production testing environment
- **Production**: Live production environment

### Quality Gates

- Code coverage requirements
- Security scan results
- Performance benchmarks
- Accessibility standards

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
