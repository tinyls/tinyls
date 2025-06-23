# Contributing to tinyls

Thank you for your interest in contributing to tinyls! This document provides guidelines and instructions for contributing to the project.

## Code of Conduct

By participating in this project, you agree to abide by our Code of Conduct. Please read it before contributing.

## How Can I Contribute?

### Reporting Bugs

- Check if the bug has already been reported in the Issues section
- Use the bug report template when creating a new issue
- Include detailed steps to reproduce the bug
- Include screenshots if applicable
- Specify your environment (OS, browser, etc.)

### Suggesting Enhancements

- Check if the enhancement has already been suggested
- Use the feature request template
- Provide a clear description of the enhancement
- Explain why this enhancement would be useful
- Include any relevant examples or mockups

### Pull Requests

1. Fork the repository
2. Create a new branch for your feature/fix
3. Make your changes
4. Write/update tests
5. Update documentation
6. Submit a pull request

## Development Setup

### Prerequisites

- Java 21
- Node.js 18+
- Docker and Docker Compose
- Maven
- Git

### Local Development

1. Fork and clone the repository:

```bash
git clone https://github.com/yourusername/tinyls.git
cd tinyls
```

2. Set up environment variables:

```bash
cp .env.example .env
# Edit .env with your configuration
```

3. Start the development environment:

```bash
docker compose watch
```

## Coding Standards

### Backend (Java)

- Follow the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Write unit tests for new features
- Document public APIs
- Keep methods small and focused
- Use proper exception handling

### Frontend (React)

- Follow the [Airbnb JavaScript Style Guide](https://github.com/airbnb/javascript)
- Use functional components with hooks
- Write unit tests for components
- Keep components small and reusable
- Use TypeScript for type safety
- Follow React best practices

## Testing

### Backend Tests

```bash
cd url-shortener
./mvnw test
```

### Frontend Tests

```bash
cd frontend
npm test
```

### End-to-End Tests

```bash
npm run test:e2e
```

## Documentation

- Update README.md if needed
- Document new features
- Update API documentation
- Add comments to complex code
- Keep documentation up to date

## Commit Messages

Follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

Types:

- feat: New feature
- fix: Bug fix
- docs: Documentation changes
- style: Code style changes
- refactor: Code refactoring
- test: Adding/updating tests
- chore: Maintenance tasks

## Review Process

1. All pull requests require at least one review
2. CI checks must pass
3. Code coverage should not decrease
4. Documentation must be updated
5. Tests must be added/updated

## Release Process

1. Create a release branch
2. Update version numbers
3. Update documentation
4. Run tests
5. Create release notes
6. Tag release
7. Deploy to staging
8. Deploy to production

## Getting Help

- Check the [documentation](docs/)
- Join our [community chat](https://gitter.im/tinyls/community)
- Open an issue for bugs or feature requests

## License

By contributing to tinyls, you agree that your contributions will be licensed under the project's [MIT License](LICENSE).
