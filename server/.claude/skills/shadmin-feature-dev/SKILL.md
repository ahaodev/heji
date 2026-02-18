---
name: shadmin-feature-dev
description: Apply Shadmin feature-development standards (backend Go/Gin/Ent + frontend React/TS). Use when adding/modifying features, CRUD modules, API routes/controllers/usecases/repositories, Ent schemas, or web pages/routes.
---

# Shadmin Feature Development

## Purpose
When the user asks to add/modify a feature in this repo, follow Shadmin’s established architecture and conventions, and land code in the correct layers with minimal, verifiable changes.

## Mandatory rules
1. **Always follow**: [AI_RULES.md](../../../AI_RULES.md)
2. **Use the existing layering**:
   - Backend: `api (controller/route) → usecase → repository → ent/schema`
   - Frontend: `web/src/services → web/src/features/<feature> → web/src/routes`
3. **Permissions must be consistent**:
   - Protected APIs must use JWT + Casbin `CheckAPIPermission()` for `/api/v1/system/*` routes.
4. **Response format must be consistent**:
   - Use `domain.RespSuccess(...)` / `domain.RespError(...)`.
5. **Finish with verification**:
   - Backend: `go test ./...`
   - If frontend changed: `cd web && (npm|pnpm) run lint && (npm|pnpm) run format:check`

## Workflow (apply every time)
1. **Clarify scope** (what to do / not do) + list APIs, data model, permissions, and UI impact.
2. **List touched files first** (grouped by backend/frontend layers) before editing.
3. Implement changes **layer-by-layer**:
   - `domain/` (DTOs + interfaces + errors)
   - `ent/schema/` (if data model changes)
   - `repository/`
   - `usecase/` (timeouts, validation, orchestration)
   - `api/controller/` (HTTP only + swagger comments)
   - `api/route/` + `api/route/factory.go` (wiring)
   - `web/src/types` + `web/src/services` + `web/src/features` + `web/src/routes`
4. **Output at the end**:
   - Change summary
   - File list
   - How to verify

## References (optional)
- Prompt template: [AI提示词模板.md](../../../AI提示词模板.md)
- Repo guidelines: [AGENTS.md](../../../AGENTS.md)

## Examples (triggers)
- “新增项目管理模块（Project CRUD）”
- “给字典增加一个字段并在前端表格显示”
- “新增 /api/v1/system/xxx 接口并接入菜单权限”
