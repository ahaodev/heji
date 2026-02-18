# AI_RULES.md

> 目标：当 AI 在本仓库中“新增/修改功能”时，必须按本规则落代码，保证 **分层一致、权限一致、接口一致、可维护**。

---

## 0. 总原则（必须遵守）

1. **最小改动**：只改与需求相关的文件与代码；避免“顺手重构”。
2. **分层清晰**：后端严格遵循 `API → Usecase → Repository → DB(Ent)`，领域模型与接口在 `domain/`。
3. **接口稳定**：保持现有 API 风格：
   - BasePath 固定：`/api/v1`（见 `api/route/route.go`）
   - 统一响应：`domain.RespSuccess(...) / domain.RespError(...)`
   - 控制器使用 Gin：`ShouldBindJSON` + `Query/Param` + 合理 HTTP 状态码
4. **权限一致**：
   - 受保护路由必须走 JWT + Casbin（路径+方法）权限校验。
   - 系统管理类功能默认放在 `/api/v1/system/*` 并使用 `casbinMiddleware.CheckAPIPermission()`（见 `api/route/system_routes.go`）。
5. **可验证**：改完必须能运行/编译，并执行与改动相关的最小验证：`go test ./...`（以及前端 `npm/pnpm` 的 lint/format 若你动了前端）。

---

## 1. 开发流程（每次加功能都按这个顺序）

### 1.1 需求澄清（先写清楚再写代码）
在开始编码前，必须明确并输出（在你的回答或计划中）：
- 功能边界：做什么 / 不做什么
- 后端：需要哪些实体/字段、哪些 API（路径、方法、请求/响应示例）、是否需要分页/筛选
- 权限：哪些接口需要权限、权限点如何命名/如何在菜单中呈现
- 前端：新增哪些页面/表格/表单/弹窗、路由地址、与哪些 API 交互

### 1.2 落地清单（先列文件，再编码）
每次实现功能前，先列出你将要触及的文件路径（按层次），例如：
- `domain/...`
- `ent/schema/...`
- `repository/...`
- `usecase/...`
- `api/controller/...`
- `api/route/...`（以及可能的 `api/route/factory.go`）
- `web/src/types/...`
- `web/src/services/...`
- `web/src/features/<feature>/...`
- `web/src/routes/...`

---

## 2. 后端规则（Go + Gin + Ent）

### 2.1 目录与职责（必须按职责放文件）
- `domain/`：
  - 领域实体（Entity/DTO）、错误（`var Err...`）、常量
  - Repository 接口、UseCase 接口
  - 请求/响应结构体（如 `CreateXxxRequest`、`UpdateXxxRequest`、`QueryParams` 等）
- `ent/schema/`：
  - 数据库 schema（新增字段/实体必须先改 schema 再生成）
- `repository/`：
  - 使用 Ent 的具体实现（`NewXxxRepository(...)`）
  - Domain ↔ Ent 的转换函数（必要时）
- `usecase/`：
  - 业务编排、校验、事务（如需要）
  - 必须使用 `context.WithTimeout`（见现有 usecase 模式）
- `api/controller/`：
  - HTTP 解析与返回（不要塞业务逻辑）
  - Swagger 注释（与现有风格一致）
- `api/route/`：
  - 路由注册、鉴权中间件挂载
  - 新增 Controller 的创建要走 `ControllerFactory`

### 2.2 新增一个“资源模块”的标准步骤（强制模板）
当新增一个类似 User/Role/Dict 的管理模块时，按以下步骤：

1) **domain 层**（先写接口与 DTO）：
- 新增：`domain/<resource>.go`
- 包含：
  - `type <Resource> struct { ... }`
  - `type Create<Resource>Request struct { ... }`
  - `type Update<Resource>Request struct { ... }`（建议字段用指针表示可选）
  - `type <Resource>QueryFilter/QueryParams struct { ...; domain.QueryParams }`
  - `type <Resource>Repository interface { ... }`
  - `type <Resource>UseCase interface { ... }`
  - 必要错误：`var Err<Resource>NotFound = errors.New("...")` 等

2) **Ent schema**（新增/修改 DB 结构）：
- 新增：`ent/schema/<resource>.go`
- 变更后必须：
  - 运行 `go generate ./ent`（或项目约定的 ent 生成命令）
  - 若新增 API 影响 swagger，后续再跑 `go generate`/`swag`（按项目已有方式）

3) **repository 实现**：
- 新增：`repository/<resource>_repository.go`
- 规则：
  - 仅做数据读写 + 必要的转换
  - 支持分页/排序时：使用 `domain.ValidateQueryParams(&filter.QueryParams)` 的既有模式
  - 不要在 repository 写 HTTP/权限逻辑

4) **usecase 实现**：
- 新增：`usecase/<resource>_usecase.go`
- 规则：
  - `ctx, cancel := context.WithTimeout(c, timeout)`
  - 业务校验放 usecase（比如状态枚举、唯一性、跨表校验）
  - 错误用 `%w` 包装：`fmt.Errorf("...: %w", err)`

5) **controller 实现**：
- 新增：`api/controller/<resource>_controller.go`
- 规则：
  - 请求解析：`ShouldBindJSON` / `Query` / `Param`
  - 返回：`c.JSON(http.StatusOK|Created|BadRequest|NotFound|InternalServerError, domain.Resp...)`
  - 分页参数保持一致：`page` / `page_size`
  - Swagger 注释遵循现有文件风格（@Summary/@Description/@Tags/@Router 等）

6) **route 注册**：
- 修改：`api/route/system_routes.go`（系统管理类）或对应 public/protected routes
- 规则：
  - 受保护接口：在 group 上挂 `casbinMiddleware.CheckAPIPermission()`
  - REST 风格优先：
    - `GET /system/<resource>` 列表
    - `POST /system/<resource>` 创建
    - `GET /system/<resource>/:id` 详情
    - `PUT /system/<resource>/:id` 更新
    - `DELETE /system/<resource>/:id` 删除

7) **factory 注入**：
- 修改：`api/route/factory.go`
- 规则：
  - 创建 `<Resource>Repository` 与 `<Resource>UseCase` 并注入 `<Resource>Controller`
  - 依赖必须从现有 `f.db / f.app / f.timeout` 获取，不要引入新的全局变量

### 2.3 权限与菜单（后端）
- 新增受保护 API 时：
  - 需要可被 Casbin 校验（路径+方法）
  - 路由必须在 protected routes 下注册
- 若功能需要出现在侧边栏/菜单：
  - 按项目现有“菜单来自后端 resources”的机制接入（不要在前端硬编码菜单树）

---

## 3. 前端规则（React + TS + Vite + TanStack Router/Query）

### 3.1 目录与职责
- `web/src/services/`：API 封装（与后端 endpoints 一一对应）
  - 使用 `apiClient`
  - 返回 `response.data.data`
- `web/src/types/`：跨模块复用的类型定义（或与现有类型文件保持一致）
- `web/src/features/<feature>/`：功能模块（页面、组件、hooks、schema）
- `web/src/routes/`：路由文件（TanStack Router 的 file-based route）

### 3.2 新增一个页面/模块的标准步骤
1) **types**：补齐请求/响应/实体类型（尽量与后端 domain 字段对齐）
2) **services**：新增 `xxxApi.ts`：
   - `getXxx(params)` / `createXxx(data)` / `updateXxx(id,data)` / `deleteXxx(id)`
3) **feature 模块**：
   - `web/src/features/<feature>/index.tsx`：页面入口
   - `components/`：表格、弹窗、表单
   - `hooks/`：TanStack Query hooks（`useXxx`/`useCreateXxx` 等）
   - `data/schema.ts`：zod schema（表单校验/URL search 校验）
4) **routes**：新增路由文件并引用 feature 入口组件
5) **权限**：
   - 有权限控制的按钮/区域必须用现有 `PermissionButton` / `PermissionGuard`

### 3.3 与后端分页/筛选对齐
- URL search 与后端 query 参数统一（如 `page/page_size/status/search` 等）。
- 将路由 search schema（zod）与 API query 参数映射写清楚，避免“前端字段名与后端不一致”。

### 3.4 错误处理与体验
- API 报错：尽量使用项目已有的错误处理工具（如 `handle-server-error`）或沿用现有页面的处理方式。
- 表格加载：提供 loading skeleton（参考 `features/system/users`）。

---

## 4. 质量门禁（必须通过）

### 4.1 后端
- `go fmt ./...`
- `go vet ./...`
- `go test ./...`
- 如改了 Ent schema：`go generate ./ent`
- 如改了 swagger 注释或新增接口：按项目方式更新 swagger（`go generate` / `swag init`）

### 4.2 前端（当且仅当你改了 web/ 代码）
在 `web/` 目录下执行项目已有命令：
- `npm run lint`（或 `pnpm run lint`）
- `npm run format:check`（或 `pnpm run format:check`）
- `npm run build`（可选但推荐，尤其是改了路由/构建相关）

---

## 5. 变更提交的输出格式

当你实现完一个功能，你必须输出：
1. **改动摘要**（1-5 条）
2. **涉及的文件列表**（按后端/前端分组）
3. **如何验证**（命令 + 关键路径，如访问某个页面/调用某个 API）

---

## 6. 功能开发提示词模板（强制用这个结构提需求给 AI）

把下面模板复制到你的需求里，让 AI 按规则落代码：

### 功能描述
- [一句话说明要做什么]

### 后端
- 新增/修改实体：
  - 实体名：
  - 字段：
- API：
  - `GET /api/v1/...`（查询参数）
  - `POST /api/v1/...`（请求体）
  - `PUT /api/v1/.../:id`
  - `DELETE /api/v1/.../:id`
- 权限：
  - 是否受保护：是/否
  - 是否走 `/system`：是/否

### 前端
- 路由：`/_authenticated/...`
- 页面：列表/详情/表单/弹窗
- 表格字段：
- 表单校验：

### 验证方式
- 后端：`go test ./...`
- 前端：`cd web && npm run lint && npm run format:check`（若有改动）

---

## 7. 禁止事项
- 不允许把业务逻辑塞进 controller/route。
- 不允许绕过 Casbin 在 protected API 上“裸奔”。
- 不允许新增不必要的第三方库（前端/后端都一样）。
- 不允许一次改动触碰大量无关文件。
- 不允许改变既有 API 响应结构（除非需求明确）。
