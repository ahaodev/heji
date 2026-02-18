# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Shadmin is a full-stack admin dashboard system with Role-Based Access Control (RBAC). It consists of a Go backend with Gin framework and a React frontend built with modern technologies.

## Architecture

The project follows clean architecture principles with clear separation of concerns:

- **api/**: HTTP handlers and routing logic
- **cmd/**: Application entry point and command-line interface
- **domain/**: Business entities and domain models
- **usecase/**: Business logic and use cases
- **repository/**: Data access layer implementations
- **ent/**: Database schema definitions using Ent ORM
- **internal/**: Internal utilities and initializers
- **pkg/**: Shared packages and utilities
- **web/**: React frontend application
- **bootstarp/**: Application bootstrapping and configuration (note: directory name has typo)

## Backend Stack

- **Language**: Go 1.24 (minimum required version)
- **Framework**: Gin web framework
- **ORM**: Ent (Facebook's entity framework for Go)
- **Authentication**: JWT tokens with access/refresh token pattern
- **Authorization**: Casbin for RBAC permissions
- **Databases**: PostgreSQL, MySQL, SQLite (configurable via DB_TYPE)
- **File Storage**: Local disk, S3, or MinIO (configurable via STORAGE_TYPE)
- **Documentation**: Swagger/OpenAPI with swaggo
- **Logging**: Logrus with file rotation

## Frontend Stack

- **Framework**: React 19 with TypeScript
- **Build Tool**: Vite
- **UI Library**: Shadcn UI primitives with Tailwind CSS
- **State Management**: Zustand
- **Data Fetching**: TanStack Query (React Query)
- **Routing**: TanStack Router
- **Forms**: React Hook Form with Zod validation
- **Charts**: Recharts

## Development Commands

### Backend Development

```bash
# Run the application in development mode
go run main.go

# Run with specific database type
DB_TYPE=sqlite go run main.go

# Generate Swagger documentation
go generate ./...
# Or specifically: swag init -g main.go --output ./docs

# Run tests
go test ./...

# Build the application
go build -o shadmin main.go

# Generate Ent schemas (from ent/ directory)
go generate ./...
# Or specifically: go run -mod=mod entgo.io/ent/cmd/ent generate ./schema
```

### Frontend Development

```bash
# Navigate to web directory first
cd web

# Install dependencies
pnpm install

# Run development server (runs on port 5173, proxies /api and /share to backend on port 55667)
pnpm run dev

# Build for production
pnpm run build

# Type checking
tsc -b

# Linting
pnpm run lint

# Format code (write)
pnpm run format

# Check formatting
pnpm run format:check

# Preview production build
pnpm run preview

# Run knip for unused dependencies
pnpm run knip
```

### Docker Development

```bash
# Build multi-stage Docker image
docker build -t shadmin .

# Run with Docker (uses SQLite by default)
docker run -d --name shadmin -p 55667:55667 shadmin

# Run with volume mounts for persistence
docker run -d --name shadmin \
  -p 55667:55667 \
  -v ./database:/app/database \
  -v ./uploads:/app/uploads \
  -v ./logs:/app/logs \
  shadmin
```

## Configuration

The application uses environment variables for configuration. Copy `.env.example` to `.env` and modify as needed:

- **APP_ENV**: development/production
- **CONTEXT_TIMEOUT**: Request timeout in seconds (default: 60)
- **PORT**: Server port (default: :55667)
- **DB_TYPE**: sqlite/postgres/mysql
- **DB_DSN**: Database connection string (leave empty for SQLite default)
- **ACCESS_TOKEN_EXPIRY_HOUR**: Access token expiration hours (default: 3)
- **REFRESH_TOKEN_EXPIRY_HOUR**: Refresh token expiration hours (default: 24)
- **ACCESS_TOKEN_SECRET**: Secret for signing access tokens
- **REFRESH_TOKEN_SECRET**: Secret for signing refresh tokens
- **ADMIN_USERNAME**, **ADMIN_PASSWORD**, **ADMIN_EMAIL**: Default admin user credentials
- **STORAGE_TYPE**: disk/s3/minio
- **STORAGE_BASE_PATH**: Local storage path (default: ./uploads)
- **S3_ADDRESS**, **S3_ACCESS_KEY**, **S3_SECRET_KEY**, **S3_BUCKET**, **S3_TOKEN**: S3/MinIO configuration

## Key Architecture Patterns

### Clean Architecture Layers
- **Domain**: Core business entities (User, Role, Menu, etc.)
- **Usecase**: Business logic orchestration
- **Repository**: Data persistence interfaces and implementations
- **API**: HTTP transport layer with Gin handlers

### Authentication & Authorization
- JWT-based authentication with access/refresh tokens
- Casbin RBAC for fine-grained permissions
- Middleware-based route protection

### Database Design
- Ent ORM with code generation from Go schemas
- Support for multiple database backends
- Automatic migrations and schema updates
- Indexed fields for performance (username, email, status)

### File Management
- Pluggable storage backends (local disk, S3, MinIO)
- Upload size limits and type validation
- Secure file serving with access controls

## API Structure

- **Base Path**: `/api/v1`
- **Public Routes**: `/auth/*` (login, registration)
- **Protected Routes**: Require JWT authentication
  - `/profile/*`: User profile management
  - `/resources/*`: Menu and resource access
  - `/system/*`: Admin-only system management
- **Documentation**: Available at `/swagger/` in development

## Testing Strategy

The project uses Go's standard testing framework. Run tests with:
```bash
# Run all tests
go test ./...

# Run tests with coverage
go test -cover ./...

# Run tests in verbose mode
go test -v ./...
```

For frontend testing, the React app uses standard testing tools available through Vite. The project structure suggests testing files should follow Go conventions (`*_test.go` files).

## Deployment

The application is designed for containerized deployment:

1. **Multi-stage Docker build** optimizes image size
2. **UPX compression** further reduces binary size
3. **Drone CI/CD** automates builds and deployments
4. **Volume mounts** persist data, uploads, and logs
5. **Health checks** via HTTP endpoints

## Development Workflow

1. **Backend changes**: Use cases and data interfaces are defined in the domain layer and implemented in the usecase and repository layers
2. **Frontend changes**: Work in `web/` directory, use modern React patterns with TypeScript
3. **Database changes**: Update Ent schemas in `ent/schema/`, then run `go generate ./...` from the project root
4. **API changes**: Update Swagger comments in Go handlers, then run `go generate ./...` or `swag init`
5. **Code quality**: Run `pnpm run lint` and `pnpm run format:check` for frontend, `go test ./...` for backend
6. **Testing**: Verify both backend and frontend functionality before commits

## Version Management

The application supports build-time version injection:
- Version info is injected during build via Go's `-ldflags`
- Check `cmd/main.go` for version variables: `version`, `commit`, `date`
- CI/CD pipeline automatically injects version information during builds

## Important Notes
- Keep It Simple, Stupid (KISS principle)
- If the problem is too broad, please first provide an analysis and confirm with me before proceeding.
- The `bootstarp/` directory has a typo in its name (should be `bootstrap/`)
- Main entry point is `main.go` which calls `cmd.Run()` from `cmd/main.go`
- Version information is injected at build time via ldflags: `version`, `commit`, `date`
- Default admin credentials are configured via environment variables

# important-instruction-reminders
Do what has been asked; nothing more, nothing less.
NEVER create files unless they're absolutely necessary for achieving your goal.
ALWAYS prefer editing an existing file to creating a new one.
NEVER proactively create documentation files (*.md) or README files. Only create documentation files if explicitly requested by the User.