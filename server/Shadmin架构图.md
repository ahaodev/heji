# Shadmin 项目架构图

## 🏗️ 系统总体架构

```mermaid
graph TB
    %% 用户层
    subgraph "👥 用户层 (Client Layer)"
        U1[管理员用户]
        U2[普通用户]
        U3[访客用户]
    end

    %% 前端层
    subgraph "🌐 前端层 (Frontend Layer)"
        subgraph "React 应用 (web/)"
            FE1[React 19 + TypeScript]
            FE2[Vite 构建工具]
            FE3[Shadcn UI + Tailwind CSS]
            FE4[TanStack Router]
            FE5[TanStack Query]
            FE6[Zustand 状态管理]
            FE7[React Hook Form + Zod]
        end
    end

    %% API Gateway/路由层
    subgraph "🚪 API Gateway (api/)"
        subgraph "HTTP 路由 (route/)"
            R1[公共路由 /auth/*]
            R2[用户路由 /system/users/*]
            R3[角色路由 /system/roles/*]
            R4[菜单路由 /system/menus/*]
            R5[API资源路由 /system/api-resources/*]
        end

        subgraph "控制器层 (controller/)"
            C1[AuthController]
            C2[UserController]
            C3[RoleController]
            C4[MenuController]
            C5[ApiResourceController]
            C6[FileController]
        end
    end

    %% 中间件层
    subgraph "🛡️ 中间件层 (middleware/)"
        M1[JWT 认证中间件]
        M2[Casbin 权限中间件]
        M3[CORS 中间件]
        M4[日志中间件]
        M5[限流中间件]
    end

    %% 业务逻辑层
    subgraph "💼 业务逻辑层 (usecase/)"
        UC1[UserUseCase]
        UC2[RoleUseCase]
        UC3[MenuUseCase]
        UC4[AuthUseCase]
        UC5[FileUseCase]
    end

    %% 领域层
    subgraph "🎯 领域层 (domain/)"
        D1[User 实体]
        D2[Role 实体]
        D3[Menu 实体]
        D4[Auth 实体]
        D5[File 实体]
        D6[Repository 接口]
        D7[UseCase 接口]
    end

    %% 数据访问层
    subgraph "🗄️ 数据访问层 (repository/)"
        REPO1[UserRepository]
        REPO2[RoleRepository]
        REPO3[MenuRepository]
        REPO4[FileRepository]
    end

    %% ORM层
    subgraph "🔧 ORM层 (ent/)"
        ENT1[Ent Schema 定义]
        ENT2[代码生成器]
        ENT3[数据库迁移]
    end

    %% 基础设施层
    subgraph "🔧 基础设施层"
        subgraph "数据库"
            DB1[(PostgreSQL)]
            DB2[(MySQL)]
            DB3[(SQLite)]
        end

        subgraph "存储"
            S1[本地磁盘存储]
            S2[AWS S3]
            S3[MinIO]
        end

        subgraph "权限管理"
            AUTH1[Casbin RBAC]
            AUTH2[JWT Token]
        end

        subgraph "日志与监控"
            L1[Logrus 日志]
            L2[文件轮转]
        end
    end

    %% 配置与启动层
    subgraph "⚙️ 配置与启动 (bootstarp/)"
        BOOT1[环境配置]
        BOOT2[数据库初始化]
        BOOT3[依赖注入]
        BOOT4[服务启动]
    end

    %% 工具层
    subgraph "🛠️ 工具层"
        subgraph "内部工具 (internal/)"
            I1[Casbin 管理器]
            I2[Token 工具]
            I3[密码工具]
            I4[验证工具]
        end

        subgraph "公共工具 (pkg/)"
            P1[通用工具]
            P2[常量定义]
        end

        subgraph "命令行 (cmd/)"
            CMD1[应用启动]
            CMD2[版本管理]
        end
    end

    %% 连接关系
    U1 & U2 & U3 --> FE1
    FE1 --> R1 & R2 & R3 & R4 & R5

    R1 --> C1
    R2 --> C2
    R3 --> C3
    R4 --> C4
    R5 --> C5

    C1 & C2 & C3 & C4 & C5 --> M1 & M2
    C1 --> UC4
    C2 --> UC1
    C3 --> UC2
    C4 --> UC3
    C5 --> UC5

    UC1 & UC2 & UC3 & UC4 & UC5 --> D6
    D6 --> REPO1 & REPO2 & REPO3 & REPO4

    REPO1 & REPO2 & REPO3 & REPO4 --> ENT1
    ENT1 --> DB1 & DB2 & DB3

    UC5 --> S1 & S2 & S3
    M1 --> AUTH2
    M2 --> AUTH1

    BOOT1 --> BOOT2 --> BOOT3 --> BOOT4
    BOOT4 --> CMD1

    %% 样式定义
    classDef frontend fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    classDef api fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef middleware fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef usecase fill:#e8f5e8,stroke:#1b5e20,stroke-width:2px
    classDef domain fill:#fce4ec,stroke:#880e4f,stroke-width:2px
    classDef repo fill:#f1f8e9,stroke:#33691e,stroke-width:2px
    classDef infra fill:#eceff1,stroke:#263238,stroke-width:2px

    class FE1,FE2,FE3,FE4,FE5,FE6,FE7 frontend
    class R1,R2,R3,R4,R5,C1,C2,C3,C4,C5,C6 api
    class M1,M2,M3,M4,M5 middleware
    class UC1,UC2,UC3,UC4,UC5 usecase
    class D1,D2,D3,D4,D5,D6,D7 domain
    class REPO1,REPO2,REPO3,REPO4 repo
    class DB1,DB2,DB3,S1,S2,S3,AUTH1,AUTH2,L1,L2 infra
```

## 🔄 数据流向图

```mermaid
sequenceDiagram
    participant U as 👤 用户
    participant F as 🌐 前端 (React)
    participant R as 🚪 路由层
    participant M as 🛡️ 中间件
    participant C as 📋 控制器
    participant UC as 💼 UseCase
    participant REPO as 🗄️ Repository
    participant DB as 💾 数据库
    participant CASBIN as 🔐 Casbin

    U->>F: 1. 用户操作
    F->>R: 2. HTTP 请求
    R->>M: 3. 中间件处理
    M->>CASBIN: 4. 权限验证
    CASBIN-->>M: 5. 权限结果
    M->>C: 6. 请求转发
    C->>UC: 7. 业务逻辑调用
    UC->>REPO: 8. 数据访问
    REPO->>DB: 9. 数据库操作
    DB-->>REPO: 10. 返回数据
    REPO-->>UC: 11. 返回结果
    UC-->>C: 12. 业务结果
    C-->>R: 13. HTTP 响应
    R-->>F: 14. JSON 数据
    F-->>U: 15. 界面更新
```

## 🎯 核心功能架构

### 🔐 认证授权架构
```mermaid
graph LR
    subgraph "认证流程"
        A1[用户登录] --> A2[JWT 生成]
        A2 --> A3[Access Token]
        A2 --> A4[Refresh Token]
    end

    subgraph "授权流程"
        B1[请求验证] --> B2[JWT 解析]
        B2 --> B3[Casbin 权限检查]
        B3 --> B4[API 权限验证]
        B4 --> B5[资源访问]
    end

    A3 --> B1
    A4 --> B1
```

### 📊 RBAC 权限模型
```mermaid
erDiagram
    USER ||--o{ USER_ROLE : has
    ROLE ||--o{ USER_ROLE : assigned
    ROLE ||--o{ ROLE_PERMISSION : has
    PERMISSION ||--o{ ROLE_PERMISSION : granted
    MENU ||--o{ PERMISSION : contains
    API ||--o{ PERMISSION : requires

    USER {
        string id PK
        string username
        string email
        string status
        datetime created_at
    }

    ROLE {
        string id PK
        string name
        string type
        string status
        datetime created_at
    }

    PERMISSION {
        string id PK
        string resource
        string action
        string effect
    }

    MENU {
        string id PK
        string name
        string path
        string parent_id
        int sort_order
    }

    API {
        string id PK
        string path
        string method
        string group
        string description
    }
```

## 🚀 部署架构

```mermaid
graph TB
    subgraph "🐳 Docker 容器"
        subgraph "应用容器"
            APP[Shadmin 应用]
            STATIC[静态文件服务]
        end

        subgraph "数据容器"
            PG[(PostgreSQL)]
            REDIS[(Redis 缓存可选)]
        end

        subgraph "存储容器"
            MINIO[MinIO 对象存储]
        end
    end

    subgraph "🔄 CI/CD"
        GIT[Git Repository] --> DRONE[Drone CI]
        DRONE --> BUILD[构建镜像]
        BUILD --> REGISTRY[镜像仓库]
        REGISTRY --> DEPLOY[自动部署]
    end

    subgraph "🌐 负载均衡"
        LB[负载均衡器]
        LB --> APP
    end

    subgraph "📊 监控告警"
        MONITOR[应用监控]
        LOGS[日志收集]
        ALERT[告警通知]
    end

    APP --> PG
    APP --> REDIS
    APP --> MINIO
    APP --> LOGS
    MONITOR --> ALERT
```

## 📝 技术栈总览

### 后端技术栈
- **🔧 核心框架**: Go 1.24 + Gin
- **🗄️ 数据库**: Ent ORM (PostgreSQL/MySQL/SQLite)
- **🔐 权限**: Casbin RBAC + JWT
- **📁 存储**: 本地/S3/MinIO
- **📖 文档**: Swagger/OpenAPI
- **📊 日志**: Logrus

### 前端技术栈
- **⚛️ 核心框架**: React 19 + TypeScript
- **🎨 UI 框架**: Shadcn UI + Tailwind CSS
- **🔧 构建工具**: Vite
- **🌐 路由**: TanStack Router
- **📊 状态管理**: Zustand
- **🔄 数据获取**: TanStack Query
- **📋 表单**: React Hook Form + Zod

## 🔍 项目特点

1. **🏗️ 清洁架构**: 严格的分层设计，依赖倒置原则
2. **🔐 安全优先**: JWT + RBAC 双重安全保障
3. **📱 现代技术栈**: 使用最新的 React 19 和 Go 1.24
4. **🚀 高性能**: Ent ORM + Gin 框架组合
5. **🔧 可扩展**: 支持多数据库、多存储后端
6. **📊 可观测**: 完整的日志和监控支持
7. **🐳 容器化**: Docker 多阶段构建，支持容器化部署

---
**生成时间**: 2025-09-14
**版本**: v1.0
**维护者**: Shadmin Team