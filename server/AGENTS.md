# Repository Guidelines

## Project Structure & Module Organization
- Backend (Go): `api/` (routes, controllers), `bootstarp/` (env, DB, app wiring), `domain/` (core models/contracts), `repository/` (data access), `usecase/` (application logic), `internal/` (libs), `pkg/` (utilities), `ent/` (ORM codegen), `cmd/` (app entry wiring), `main.go` (entrypoint), `docs/` (Swagger), `.env(.example)`.
- Frontend (Vite + React): `web/` (source, build, lint config). Built SPA served via `web/web.go` from `web/dist/`.

## Build, Test, and Development Commands
- Backend run (dev): `go run .` (uses `:55667` by default; `.env` auto-generated if missing).
- Backend build: `go build -o shadmin .`
- Tests: `go test ./...` (coverage: `go test ./... -cover`)
- Ent schema codegen: `go generate ./ent` (edit `ent/schema/*` then regenerate)
- Swagger: `go generate` (requires `swag` CLI; install: `go install github.com/swaggo/swag/cmd/swag@latest`)
- Frontend dev: `cd web && npm i && npm run dev` (or `pnpm i && pnpm dev`)
- Frontend build: `cd web && npm run build` (outputs to `web/dist/`)

## Coding Style & Naming Conventions
- Go: run `go fmt ./...` and `go vet ./...` before commits. Package names lower_snake; exported identifiers use PascalCase; receivers short, meaningful.
- Files and folders: lower_snake (e.g., `loginlog_repository.go`). Keep layers: API → Usecase → Repository.
- Frontend: run `npm run lint` and `npm run format`. Use TypeScript, functional components, and kebab-case file names under `web/src/`.

## Testing Guidelines
- Backend: standard `testing` package; place tests alongside code (e.g., `internal/casbin/manager_test.go`). Aim for meaningful coverage on usecases and permission logic.
- Frontend: no unit tests configured; ensure lint passes and validate key flows manually after builds.

## Commit & Pull Request Guidelines
- Commits: follow Conventional Commits (e.g., `feat: add role routes`, `fix: token expiry check`). Keep subject ≤ 72 chars; body explains why/what.
- Branches: `feat/*`, `fix/*`, `chore/*`, `docs/*`.
- PRs: include summary, screenshots for UI changes, linked issues (e.g., `Closes #123`), steps to test, and any schema or env changes.

## Security & Configuration Tips
- Secrets live in `.env`; never commit real credentials. Default port `:55667`. DB defaults to SQLite; set `DB_TYPE=postgres|mysql` and `DB_DSN` for PostgreSQL or MySQL. For file storage, choose `STORAGE_TYPE=disk|minio` and configure S3 settings when using MinIO.

