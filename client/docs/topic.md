# MQTT 主题设计

## 核心问题

当前所有同步消息都发布到 `heji/book/{bookId}/sync`，客户端也订阅同一主题，导致：
1. **自己发的消息自己收到** — 需要在 `SyncReceiver` 过滤 `senderId`
2. **单主题承载过多职责** — 账本/账单/图片/ACK 全部混在一起
3. **只订阅当前账本** — 切换账本后旧账本的同步消息丢失
4. **无图片同步通道** — 图片上传/下载走 HTTP，但同步状态无通知机制

## 主题设计 — 上行/下行分离

### 设计原则

将主题按**数据流方向**划分：
- **上行（uplink）**：客户端 → 服务端，推送本地变更
- **下行（downlink）**：服务端 → 客户端，接收远端变更和 ACK

客户端只 **publish 上行主题**、只 **subscribe 下行主题**，天然不会自发自收。

### 主题结构

```
# 上行：客户端推送变更（客户端 publish，服务端 subscribe）
heji/up/{userId}/book            # 推送账本变更
heji/up/{userId}/bill            # 推送账单变更
heji/up/{userId}/image           # 推送图片元数据变更

# 下行：服务端推送变更（服务端 publish，客户端 subscribe）
heji/down/{userId}/book          # 接收账本变更 + ACK
heji/down/{userId}/bill          # 接收账单变更 + ACK
heji/down/{userId}/image         # 接收图片同步通知 + ACK
```

### 对比方案

| 特性 | 旧方案 (bookId 单主题) | 方案A (server 收/userId 发) | 方案B (上行/下行分离) |
|------|----------------------|---------------------------|---------------------|
| 自发自收 | ❌ 需过滤 | ✅ 服务端排除 | ✅ 主题天然隔离 |
| 主题语义 | 混杂 | 按实体分 | 按方向+实体分 |
| 客户端订阅 | 每个账本一个 | 3 个 | 3 个 |
| 服务端订阅 | 每个账本一个 | 1 个通用 | `heji/up/+/+`（通配） |
| ACL 控制 | 困难 | 一般 | ✅ 精确（up 只写，down 只读） |
| 调试可读性 | 差 | 一般 | ✅ 方向一目了然 |

### 服务端订阅

```
heji/up/+/book      # 通配订阅所有用户的账本上行
heji/up/+/bill      # 通配订阅所有用户的账单上行
heji/up/+/image     # 通配订阅所有用户的图片上行
# 或简写
heji/up/#           # 订阅所有上行消息
```

服务端从 topic 解析 `userId` 和实体类型，无需消息体重复携带。

### MQTT ACL（访问控制）

```
# 客户端权限
user:{userId}:
  publish:   heji/up/{userId}/#       # 只能发自己的上行
  subscribe: heji/down/{userId}/#     # 只能收自己的下行

# 服务端权限
user:server:
  subscribe: heji/up/#               # 收所有上行
  publish:   heji/down/#             # 发所有下行
```

### 消息流转

```
发送方 A                      服务端                       接收方 B
   │                           │                           │
   │ publish                   │                           │
   │ heji/up/{A}/bill          │                           │
   │ ────────────────────►     │                           │
   │                           │ 1. 持久化                  │
   │                           │ 2. 查 book_id 成员         │
   │                           │                           │
   │         ACK               │                           │
   │ heji/down/{A}/bill        │ 转发                      │
   │ ◄────────────────────     │ heji/down/{B}/bill        │
   │                           │ ────────────────────►     │
   │                           │                           │
   │                           │        ACK                │
   │                           │ heji/down/{B}/bill        │
   │                           │ ◄────────────────────     │
```

## 消息格式

```json
{
  "id": "bt0j9l2s5bo37fcla7q0",
  "type": "ADD_BILL",
  "book_id": "book_xid",
  "content": "{...实体JSON...}",
  "timestamp": 1708234567890
}
```

### 变更说明

| 字段 | 变更 | 原因 |
|------|------|------|
| `book_id` | **新增** | 服务端根据此字段查账本成员并路由 |
| `timestamp` | **新增** | 消息排序和幂等判断 |
| `sender_id` | **移除** | 服务端从上行 topic `heji/up/{userId}/...` 解析 |
| `receiver_ids` | **移除** | 服务端根据 book_id 自动路由 |

### 消息类型

| 下行主题 | 消息类型 |
|---------|---------|
| `down/{userId}/book` | `ADD_BOOK`, `UPDATE_BOOK`, `DELETE_BOOK`, `*_ACK` |
| `down/{userId}/bill` | `ADD_BILL`, `UPDATE_BILL`, `DELETE_BILL`, `*_ACK` |
| `down/{userId}/image` | `IMAGE_UPLOADED`, `IMAGE_DELETED`, `*_ACK` |

## 同步状态机

```
NOT_SYNCED(0) ──发送──► SYNCING(3) ──收到ACK──► SYNCED(1)
                            │
                            ▼ (超时30s)
                        NOT_SYNCED(0)  ← 回退重试

SYNCED(1) ──本地修改──► UPDATED(2) ──发送──► SYNCING(3) ──收到ACK──► SYNCED(1)

SYNCED(1) ──本地删除──► deleted=1, synced=0 ──发送DELETE──► SYNCING(3) ──收到ACK──► 硬删除
```

## 图片同步

图片同步分两步：文件传输走 HTTP，元数据同步走 MQTT。

### 上传流程

```
1. 本地保存图片到 Room (synced=0)
2. HTTP POST /api/v1/image/upload → 服务端返回 onlinePath
3. 更新本地 Image.onlinePath
4. Publish to heji/up/{userId}/image:
   { "type": "IMAGE_UPLOADED", "book_id": "xx",
     "content": {"image_id":"xx", "bill_id":"xx", "online_path":"xx"} }
5. 服务端转发给其他成员 → 其他成员通过 HTTP 下载
```

### 删除流程

```
1. 本地 preDelete (deleted=1, synced=0)
2. SyncTrigger 发送 IMAGE_DELETED
3. 服务端删除文件 → 通知其他成员
4. ACK 后硬删除本地记录
```

## 客户端改动

### SyncMessage

```kotlin
@Serializable
data class SyncMessage(
    @SerialName("id") val id: String = Xid.string(),
    @SerialName("type") val type: String,
    @SerialName("book_id") val bookId: String,
    @SerialName("content") val content: String,
    @SerialName("timestamp") val timestamp: Long = System.currentTimeMillis(),
)
```

### MqttSyncClient

```kotlin
// 上行：客户端发布
private const val TOPIC_UP_BOOK  = "heji/up/%s/book"
private const val TOPIC_UP_BILL  = "heji/up/%s/bill"
private const val TOPIC_UP_IMAGE = "heji/up/%s/image"

// 下行：客户端订阅
private const val TOPIC_DOWN_BOOK  = "heji/down/%s/book"
private const val TOPIC_DOWN_BILL  = "heji/down/%s/bill"
private const val TOPIC_DOWN_IMAGE = "heji/down/%s/image"

fun send(message: SyncMessage) {
    val userId = Config.user.id
    val suffix = topicSuffix(message.type)  // "book" | "bill" | "image"
    val topic = String.format("heji/up/%s/%s", userId, suffix)
    mqttClient?.publish(topic, MqttMessage(message.toJson().toByteArray()).apply { qos = 1 })
}

fun subscribeTopics() {
    val userId = Config.user.id
    mqttClient?.subscribe(String.format(TOPIC_DOWN_BOOK, userId), 1)
    mqttClient?.subscribe(String.format(TOPIC_DOWN_BILL, userId), 1)
    mqttClient?.subscribe(String.format(TOPIC_DOWN_IMAGE, userId), 1)
}
```

### SyncReceiver

不再需要 `senderId` 过滤 — 下行主题天然只收他人消息和服务端 ACK。

### SyncTrigger

- `createSyncMessage()` 只传 `bookId`，不传 `toUsers`
- 其余逻辑不变

## 服务端改动

```go
// 订阅所有上行
mqttClient.Subscribe("heji/up/+/+", 1, func(client mqtt.Client, msg mqtt.Message) {
    // 从 topic 解析: heji/up/{userId}/{entity}
    parts := strings.Split(msg.Topic(), "/")
    senderID := parts[2]  // userId
    entity := parts[3]    // "book" | "bill" | "image"

    var syncMsg SyncMessage
    json.Unmarshal(msg.Payload(), &syncMsg)

    // 1. 持久化
    persist(syncMsg)

    // 2. 发送 ACK 给发送者
    ackTopic := fmt.Sprintf("heji/down/%s/%s", senderID, entity)
    publish(ackTopic, ackMsg)

    // 3. 查账本成员，转发给其他人
    members := getBookMembers(syncMsg.BookID)
    for _, uid := range members {
        if uid != senderID {
            topic := fmt.Sprintf("heji/down/%s/%s", uid, entity)
            publish(topic, syncMsg)
        }
    }
})
```

## 迁移策略

1. 服务端先同时支持新旧主题
2. 客户端升级后使用上行/下行主题
3. 稳定后移除旧 `heji/book/{bookId}/sync` 支持
