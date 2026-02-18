# Shadmin

A full‑stack admin dashboard:
- Backend: Go + Gin, Ent ORM (SQLite/PostgreSQL/MySQL), JWT + Casbin RBAC, Swagger.
- Frontend: React + TypeScript + Vite, Tailwind + shadcn/ui, TanStack Router/Query.

## Quick Links
- Repository Guidelines: AGENTS.md
- Backend Architecture: 后端架构.md
- Frontend Architecture: 前端架构.md
- Detailed Analysis: ARCHITECTURE_ANALYSIS.md

## Project Structure
- Backend (Go): api/, bootstarp/, domain/, repository/, usecase/, internal/, pkg/, ent/, cmd/, main.go
- Frontend (web): web/src, web/package.json, web/vite.config.ts (dev proxy to backend)

## Backend – Run/Build
- Prereqs: Go 1.24+
- Run (dev):
  `go run .`
  - Listens on `:55667`. `.env` is auto‑generated if missing.
- Build:
  `go build -o shadmin .`
- Tests/coverage:
  `go test ./...`
  `go test ./... -cover`
- Ent schema codegen:
  `go generate ./ent` (edit files under `ent/schema/*`)
- Swagger (requires tool):
  `go install github.com/swaggo/swag/cmd/swag@latest`
  `go generate`

## Frontend – Dev/Build
- Dev:
  `cd web && npm i && npm run dev`
- Build (served by backend embed):
  `cd web && npm run build` (outputs to `web/dist/`)

## Makefile Shortcuts
- `make help` – list targets
- `make run` / `make dev` – backend only / backend + web
- `make build` – build backend and web
- `make check` – go fmt+vet+tests and web lint/format check
- `make hooks` – enable repo git hooks, then pre-commit runs checks automatically

## Auth, Permissions, Menus
- Auth: JWT via `Authorization: Bearer <token>`; profile and permissions fetched after login.
- API permission: Casbin checks `PATH + METHOD` for protected routes.
- Menus: `/api/v1/resources` provides menu tree and permissions; frontend adapts to sidebar.

## Contributing
- Follow Conventional Commits (e.g., `feat: add role routes`, `fix: token expiry`).
- See AGENTS.md for coding style, commands, and PR requirements.
