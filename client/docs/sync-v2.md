# 合記（heji）同步设计规范

> 本文档是前后端开发的唯一准绳。所有同步相关的实现必须遵循本文档的约定。

---

## 1. 总则

### 1.1 核心原则

| # | 原则 | 说明 |
|---|------|------|
| 1 | **本地优先** | 所有 CRUD 先写入本地 Room，再异步同步到服务端 |
| 2 | **HTTP 同步** | 数据的上行（客户端→服务端）一律通过 HTTP REST API |
| 3 | **MQTT 推送** | 服务端向其他客户端推送变更通知，MQTT 仅做下行通知通道 |
| 4 | **单写者模型** | 每条账单/账本只有创建人（`crt_user`）可修改/删除，天然无并发写冲突 |
| 5 | **增量拉取兜底** | HTTP 拉取接口补偿 MQTT 推送丢失的消息，保证最终一致 |

### 1.2 数据流总览

```
 用户A (创建人)                服务端                 用户B (成员)
    │                           │                      │
    │  1. 本地 Room CRUD        │                      │
    │  2. HTTP POST/PUT/DELETE  │                      │
    │  ────────────────────►    │                      │
    │                           │  3. 持久化             │
    │  4. HTTP 200 ◄──────────  │                      │
    │     本地标记 SYNCED        │                      │
    │                           │  5. MQTT 推送通知      │
    │                           │  ──────────────────►  │
    │                           │                      │
    │                           │     6. 写入本地 Room   │
```

---

## 2. 权限模型

| 实体 | 创建人 | 账本成员 |
|------|--------|---------|
| **账本 (Book)** | 增、删、改 | 只读 + 在其下创建账单 |
| **账单 (Bill)** | 增、删、改 | 只读 |
| **图片 (Image)** | 跟随所属账单 | 跟随所属账单 |

**规则**：
- 客户端在 UI 层和 DAO 层双重校验所有权
- 服务端在 HTTP 中间件校验 JWT + 所有权，不满足返回 `403`

---

## 3. 同步状态机

只需两个状态：

```
  ┌──────────────────┐
  │  NOT_SYNCED (0)  │ ◄── 新增 / 本地修改 / 本地删除(deleted=1)
  └────────┬─────────┘
           │ HTTP 请求成功
  ┌────────▼─────────┐
  │   SYNCED (1)     │ ◄── 服务端确认 / MQTT 推送写入 / 增量拉取写入
  └──────────────────┘
```

### 3.1 字段定义

| 字段 | 类型 | 值 | 含义 |
|------|------|----|------|
| `synced` | Int | `0` | 未同步（新增/修改后待上传） |
| `synced` | Int | `1` | 已同步（服务端已确认） |
| `deleted` | Int | `0` | 未删除 |
| `deleted` | Int | `1` | 已标记删除（待同步删除到服务端） |

### 3.2 状态转换

| 操作 | 转换 | 说明 |
|------|------|------|
| 新增 | → `synced=0, deleted=0` | 写入 Room 后等待上传 |
| 修改 | `synced=1` → `synced=0` | 本地改完后等待上传 |
| 删除 | → `deleted=1, synced=0` | 软删除，等待上传 |
| HTTP 上传成功（增/改） | `synced=0` → `synced=1` | 服务端已持久化 |
| HTTP 删除成功 | `deleted=1` → **硬删除** | 从 Room 物理删除 |
| HTTP 失败 | 保持 `synced=0` | Flow 自动重试 |
| MQTT 推送写入 | → `synced=1` | 来自其他用户的变更，直接标记已同步 |
| 增量拉取写入 | → `synced=1` | 兜底拉取的数据，直接标记已同步 |

---

## 4. HTTP 同步接口

### 4.1 CRUD 接口

#### 账本 (Book)

| 操作 | 方法 | 路径 | 权限 | 成功码 |
|------|------|------|------|--------|
| 创建 | `POST` | `/api/v1/books/` | 已登录用户 | `201` |
| 更新 | `PUT` | `/api/v1/books/:id` | 仅创建人 | `200` |
| 删除 | `DELETE` | `/api/v1/books/:id` | 仅创建人 | `200` |
| 列表 | `GET` | `/api/v1/books/` | 返回我创建的 + 我参与的 | `200` |

#### 账单 (Bill)

| 操作 | 方法 | 路径 | 权限 | 成功码 |
|------|------|------|------|--------|
| 创建 | `POST` | `/api/v1/bills/` | 账本成员 | `201` |
| 更新 | `PUT` | `/api/v1/bills/:id` | 仅创建人 | `200` |
| 删除 | `DELETE` | `/api/v1/bills/:id` | 仅创建人 | `200` |
| 查询 | `GET` | `/api/v1/bills/?book_id=xx` | 账本成员 | `200` |

#### 图片 (Image)

| 操作 | 方法 | 路径 | 权限 | 成功码 |
|------|------|------|------|--------|
| 上传 | `POST` | `/api/v1/images/upload` | 账单创建人 | `201` |
| 删除 | `DELETE` | `/api/v1/images/:id` | 账单创建人 | `200` |
| 下载 | `GET` | `/api/v1/images/:id` | 账本成员 | `200` |

#### 错误码约定

| HTTP 状态码 | 含义 |
|------------|------|
| `400` | 请求参数错误 |
| `401` | 未登录 / Token 过期 |
| `403` | 无权限（非创建人尝试修改/删除） |
| `404` | 资源不存在 |
| `409` | ID 冲突（客户端生成的 Xid 重复） |

### 4.2 增量拉取接口

#### 用途

MQTT 推送是**尽力而为**的，以下场景会丢失推送：
- 客户端离线期间（地铁、飞行模式）
- MQTT broker 重启
- 网络抖动导致连接断开的瞬间

增量拉取接口让客户端主动补回缺失的变更。

#### 触发时机

| 场景 | 触发方式 |
|------|---------|
| App 冷启动 | `onCreate` 中调用 |
| MQTT 断连重连 | `onConnected` 回调中调用 |
| 用户手动下拉刷新 | 主动触发 |

不需要定时轮询 — 在线期间 MQTT 推送已覆盖实时性。

#### 请求

```
GET /api/v1/sync/changes?since={timestamp_ms}&limit=100
Authorization: Bearer {token}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `since` | Long | 是 | 毫秒时间戳，首次传 `0` 拉取全量 |
| `limit` | Int | 否 | 每页条数，默认 `100` |

#### 服务端处理逻辑

```
1. 从 JWT 提取 userId
2. 查询该用户参与的所有账本 ID 列表
3. 查询这些账本下 updated_at > since 的 book 和 bill
4. 按 updated_at ASC 排序，最多返回 limit 条
5. 已软删除的记录只返回 { _id, action: "delete" }
6. 返回结果 + 服务端当前时间 + 分页标记
```

```sql
-- 伪 SQL
SELECT * FROM bill
WHERE book_id IN ({用户参与的账本IDs})
  AND updated_at > {since}
ORDER BY updated_at ASC
LIMIT {limit}
```

#### 响应

```json
{
  "code": 0,
  "data": {
    "books": [
      {
        "_id": "book_xid_1",
        "action": "update",
        "name": "家庭账本",
        "type": "daily",
        "crt_user_id": "user_a",
        "members": ["user_a", "user_b"],
        "upd_time": "2026-02-18T06:00:00Z"
      },
      {
        "_id": "book_xid_2",
        "action": "delete"
      }
    ],
    "bills": [
      {
        "_id": "bill_xid_1",
        "action": "add",
        "book_id": "book_xid_1",
        "money": 50.0,
        "type": 1,
        "category": "午餐",
        "crt_user": "user_b",
        "time": "2026-02-18",
        "upd_time": "2026-02-18T05:30:00Z"
      },
      {
        "_id": "bill_xid_3",
        "action": "delete"
      }
    ],
    "server_time": 1739858400000,
    "has_more": true,
    "next_since": 1739855000000
  }
}
```

| 字段 | 说明 |
|------|------|
| `has_more` | `true` 表示还有更多变更，需要继续翻页 |
| `next_since` | 下一页的 `since` 值（本页最后一条的 `updated_at` 毫秒值） |
| `server_time` | 服务端当前时间戳，最后一页时客户端用此值更新 `lastSyncTime` |

#### action 判定规则（服务端）

| 条件 | action |
|------|--------|
| `deleted = true` | `"delete"` |
| `created_at = updated_at` | `"add"` |
| 其他 | `"update"` |

> 客户端对 `add` 和 `update` 均执行 `upsert`，所以 add/update 判定不精确不影响正确性。
> `delete` 必须准确。

#### 客户端处理

```kotlin
suspend fun pullAllChanges() {
    var since = dataStore.lastSyncTime  // 首次为 0（拉取全量）
    do {
        val resp = apiServer.getChanges(since, limit = 100)
        if (!resp.isSuccess) return

        val data = resp.data

        for (book in data.books) {
            when (book.action) {
                "delete" -> bookDao.hardDelete(book.id)
                else     -> bookDao.upsert(book.toEntity().copy(synced = SYNCED))
            }
        }
        for (bill in data.bills) {
            when (bill.action) {
                "delete" -> billDao.hardDelete(bill.id)
                else     -> billDao.upsert(bill.toEntity().copy(synced = SYNCED))
            }
        }

        since = data.nextSince ?: data.serverTime
    } while (data.hasMore)

    dataStore.lastSyncTime = since
}
```

#### 幂等性

- `upsert`：多次执行结果一致
- `hardDelete`：删除不存在的记录不报错
- MQTT 推送和增量拉取可能重叠（同一条数据两个渠道都收到），`upsert` 天然去重
- `lastSyncTime` 在全部处理成功后才更新，失败则下次从断点重拉

#### 典型场景

```
18:00  小明坐地铁，手机没信号，MQTT 断开
18:30  小红记了 3 笔账单（晚餐、水果、打车）
19:00  小红改了"晚餐"的金额
19:30  小红删了"水果"
       ↑ 这 5 次 MQTT 推送全部丢失

20:00  小明到家，打开 App
       → pullAllChanges(since=上次同步时间)
       → 服务端返回最新快照（不是操作历史）：
         - ADD  晚餐（已含改后金额）
         - ADD  打车
         - DELETE 水果
       → 小明数据追平
```

---

## 5. MQTT 推送通知

### 5.1 职责

MQTT **仅做下行通知通道**：
- 客户端只 **subscribe**，不 publish
- 上行全部走 HTTP
- 推送携带完整实体数据，客户端收到后直接写入 Room

### 5.2 主题

```
heji/notify/{userId}/book      # 账本变更通知
heji/notify/{userId}/bill      # 账单变更通知
heji/notify/{userId}/image     # 图片变更通知
```

### 5.3 消息格式

```json
{
  "id": "bt0j9l2s5bo37fcla7q0",
  "type": "ADD_BILL",
  "book_id": "book_xid",
  "content": "{...实体完整JSON...}",
  "timestamp": 1708234567890
}
```

### 5.4 消息类型

| 主题 | 消息类型 |
|------|---------|
| `heji/notify/{userId}/book` | `ADD_BOOK`, `UPDATE_BOOK`, `DELETE_BOOK` |
| `heji/notify/{userId}/bill` | `ADD_BILL`, `UPDATE_BILL`, `DELETE_BILL` |
| `heji/notify/{userId}/image` | `IMAGE_UPLOADED`, `IMAGE_DELETED` |

### 5.5 ACL

```
# 客户端（只订阅自己的下行主题）
user:{userId}:
  subscribe: heji/notify/{userId}/#

# 服务端（只发布到下行主题）
user:server:
  publish: heji/notify/#
```

---

## 6. 客户端实现规范

### 6.1 架构

```
┌──────────────────────────────────────────────────────┐
│                                                      │
│   ┌────────┐     ┌──────────┐     ┌──────────────┐   │
│   │   UI   │────►│  Room DB │────►│ SyncTrigger  │   │
│   │ (CRUD) │     │ (本地优先) │     │ (Flow 观察)  │   │
│   └────────┘     └──────────┘     └──────┬───────┘   │
│                                         │            │
│                                  HTTP API 调用        │
│                                         │            │
│                                  ┌──────▼───────┐    │
│                                  │  ApiServer   │    │
│                                  │  (Retrofit)  │    │
│                                  └──────────────┘    │
│                                                      │
│   ┌──────────────┐     ┌─────────────────────────┐   │
│   │  MqttClient  │────►│ SyncReceiver            │   │
│   │ (subscribe)  │     │ → Handler → Room upsert │   │
│   └──────────────┘     └─────────────────────────┘   │
│                                                      │
└──────────────────────────────────────────────────────┘
```

### 6.2 SyncTrigger — 本地变更 → HTTP 上传

观察 Room 中 `synced != 1` 的数据，通过 HTTP 上传到服务端：

```kotlin
fun observeAndSync() {
    billDao.flowNotSynced().distinctUntilChanged().collect { bills ->
        for (bill in bills) {
            val result = when {
                bill.deleted == DELETED   -> apiServer.deleteBill(bill.id)
                bill.synced == NOT_SYNCED -> apiServer.upsertBill(bill)
            }
            if (result.isSuccess) {
                if (bill.deleted == DELETED) billDao.hardDelete(bill.id)
                else billDao.updateSyncStatus(bill.id, SYNCED)
            }
        }
    }
}
```

#### flowNotSynced SQL

```sql
SELECT * FROM bill WHERE synced != 1
```

### 6.3 SyncReceiver — MQTT 通知 → 写入本地

```kotlin
fun onNotification(topic: String, message: SyncMessage) {
    when (message.type) {
        "ADD_BILL", "UPDATE_BILL" -> {
            val bill = Json.decodeFromString<Bill>(message.content)
            billDao.upsert(bill.copy(synced = SYNCED))
        }
        "DELETE_BILL" -> {
            billDao.hardDelete(message.content)
        }
        // book、image 同理
    }
}
```

### 6.4 启动同步流程

```
App 启动：
  1. pullAllChanges()       ← 增量拉取，补回离线期间的变更
  2. SyncTrigger.start()    ← 开始观察本地未同步数据并上传
  3. MqttClient.connect()   ← 连接 MQTT，开始接收实时推送

MQTT 重连时：
  1. pullAllChanges()       ← 补回断连期间的变更
```

### 6.5 lastSyncTime

```kotlin
// DataStore 持久化
var lastSyncTime: Long  // 毫秒时间戳，取自服务端 server_time
                        // 首次安装为 0，触发全量拉取
```

---

## 7. 服务端实现规范

### 7.1 HTTP Controller — 持久化后推送

以创建账单为例：

```go
func (c *BillController) CreateBill(ctx *gin.Context) {
    userID := getUserID(ctx)
    bill, err := c.BillUsecase.CreateBill(ctx, userID, &req)
    if err != nil {
        errorResponse(ctx, err)
        return
    }

    // 持久化成功后，推送通知给其他账本成员
    c.SyncService.NotifyBookMembers(bill.BookID, userID, NotifyMessage{
        Type:    "ADD_BILL",
        BookID:  bill.BookID,
        Content: toJSON(bill),
    })

    ctx.JSON(201, successResponse(bill))
}
```

**规则**：必须先持久化成功，再推送 MQTT。推送失败不影响 HTTP 响应。

### 7.2 SyncService — 纯推送

```go
type SyncService struct {
    mqttClient mqtt.Client
    bookRepo   domain.BookRepository
}

func (s *SyncService) NotifyBookMembers(bookID, senderID string, msg NotifyMessage) {
    book, err := s.bookRepo.GetByID(ctx, bookID)
    if err != nil {
        log.Errorf("Failed to get book %s: %v", bookID, err)
        return
    }
    entity := entityFromType(msg.Type)
    for _, memberID := range book.Members {
        if memberID != senderID {
            topic := fmt.Sprintf("heji/notify/%s/%s", memberID, entity)
            s.mqttClient.Publish(topic, 1, false, toJSON(msg))
        }
    }
}
```

**规则**：
- 服务端**不订阅**任何 MQTT 主题，只做 Publish
- 推送失败静默处理（客户端有增量拉取兜底）
- 排除 `senderID`，不推送给操作发起人

### 7.3 软删除

服务端 DELETE 接口改为**软删除**（设置 `deleted=true, updated_at=now()`），不做物理删除。
增量拉取接口依赖 `deleted` 字段来发现已删除的记录。

### 7.4 数据库索引

| 表 | 索引 | 用途 |
|------|------|------|
| `bill` | `(book_id, updated_at)` | 增量拉取按账本过滤+时间排序 |
| `book` | `(updated_at)` | 增量拉取按时间排序 |

---

## 8. 图片同步

图片同步分两步：**文件传输走 HTTP，元数据通知走 MQTT**。

### 8.1 上传

```
1. 本地保存图片文件 + Room 记录 (synced=0)
2. HTTP POST /api/v1/images/upload → 服务端返回 onlinePath
3. 本地更新 onlinePath, synced=1
4. 服务端推送 MQTT IMAGE_UPLOADED 通知给其他成员
5. 其他成员收到通知 → HTTP GET 下载图片
```

### 8.2 删除

```
1. 本地 deleted=1, synced=0
2. SyncTrigger → HTTP DELETE /api/v1/images/:id
3. 成功 → 硬删除本地记录
4. 服务端推送 IMAGE_DELETED 通知 → 其他成员删除本地缓存
```

---

## 9. ID 生成

| 端 | 方式 | 说明 |
|----|------|------|
| 客户端 | [Xid](https://github.com/0xShamil/java-xid) | 20 字符 base32，基于时间戳+随机值，无需中心协调 |
| 服务端 | 接受客户端 ID | 唯一性校验，冲突返回 `409` |

---

## 10. 离线与重试

| 场景 | 行为 |
|------|------|
| 无网络时 CRUD | 正常操作本地 Room，数据标记 `synced=0` |
| HTTP 上传失败 | 保持 `synced=0`，Flow 自动重试 |
| 网络不可用 | 跳过 HTTP 调用，避免无效请求 |
| 网络恢复 | Flow 自动触发上传 + `pullAllChanges()` 拉取缺失变更 |

---

## 附录：迁移要点（从 v1 纯 MQTT 方案）

开发阶段，不考虑兼容性：

| # | 变更 |
|---|------|
| 1 | 移除 MQTT 上行主题（`heji/up/{userId}/{entity}`）和 ACK 机制 |
| 2 | SyncTrigger 从 MQTT publish 改为 HTTP API 调用 |
| 3 | 状态机从 4 状态简化为 2 状态：移除 `UPDATED(2)` 和 `SYNCING(3)` |
| 4 | MQTT 主题改为 `heji/notify/{userId}/{entity}`，客户端只订阅不发布 |
| 5 | 服务端从 MQTT subscriber 改为 HTTP controller 中调用 `SyncService.NotifyBookMembers()` |
| 6 | 服务端 DELETE 改为软删除，新增增量拉取接口 |
