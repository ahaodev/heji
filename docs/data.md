# 数据说明

## 客户端

App 端使用 SQLite 数据库（通过 Room ORM 访问）。

App 数据库使用客户端生成的 Xid 作为主键，同步到服务端后主键不变。

App 离线优先，服务端仅作为多设备的同步备份。

App 首先保存数据至 SQLite，然后在后台通过 HTTP 同步至服务端。

App 在未登录情况下能够离线使用，默认创建一个本地用户和一个本地个人账本。

### 同步状态字段（所有同步实体通用）

| 字段 | 类型 | 值 | 含义 |
|------|------|----|------|
| `synced` | Int | `0` | 未同步，待上传 |
| `synced` | Int | `1` | 已同步，服务端已确认 |
| `deleted` | Int | `0` | 未删除 |
| `deleted` | Int | `1` | 已标记删除，待上传后硬删除 |

---

## 服务端

服务端使用关系型数据库（SQLite / PostgreSQL / MySQL，通过环境变量 `DB_TYPE` 配置）。

所有实体均使用客户端传入的 Xid 作为主键（`_id`），服务端不自行生成 ID。

### 用户（User）

| 列名 | 类型 | 说明 |
|------|------|------|
| `_id` | String | Xid 主键 |
| `name` | String | 用户名 |
| `password` | String | 密码（加密存储） |
| `email` | String | 邮箱 |

### 账本（Book）

| 列名 | 类型 | 说明 |
|------|------|------|
| `_id` | String | Xid 主键（客户端生成） |
| `name` | String | 账本名称 |
| `crt_user_id` | String | 创建人 ID |
| `members` | Array | 账本成员 ID 列表（含创建人） |
| `deleted` | Bool | 软删除标记 |
| `created_at` | Long | 创建时间（毫秒时间戳） |
| `updated_at` | Long | 最后修改时间（毫秒时间戳，用于增量拉取） |

### 账单（Bill）

| 列名 | 类型 | 说明 |
|------|------|------|
| `_id` | String | Xid 主键（客户端生成） |
| `book_id` | String | 所属账本 ID |
| `money` | Double | 金额 |
| `category_id` | String | 所属账单类型 ID |
| `type` | Int | 收支类型（1=支出，2=收入） |
| `dealer` | String | 经手人 |
| `crt_user` | String | 创建人 ID |
| `remark` | String | 备注 |
| `time` | String | 账单日期（用户选择的日期，yyyy-MM-dd） |
| `deleted` | Bool | 软删除标记 |
| `created_at` | Long | 创建时间（毫秒时间戳） |
| `updated_at` | Long | 最后修改时间（毫秒时间戳，用于增量拉取） |

### 账单类型（Category）

| 列名 | 类型 | 说明 |
|------|------|------|
| `_id` | String | Xid 主键（客户端生成） |
| `book_id` | String | 所属账本 ID |
| `type` | Int | 收支类型（1=支出，2=收入） |
| `name` | String | 标签名 |
| `level` | Int | 多级标签层级 |
| `index` | Int | 排序顺序 |
| `crt_user` | String | 创建人 ID |
| `deleted` | Bool | 软删除标记 |
| `created_at` | Long | 创建时间（毫秒时间戳） |
| `updated_at` | Long | 最后修改时间（毫秒时间戳，用于增量拉取） |

### 账单票据图片（BillImage）

| 列名 | 类型 | 说明 |
|------|------|------|
| `_id` | String | Xid 主键（客户端生成） |
| `bill_id` | String | 所属账单 ID |
| `filename` | String | 文件名 |
| `online_path` | String | 服务端文件路径（上传成功后写入） |
| `length` | Long | 文件大小（字节） |
| `md5` | String | 文件 MD5，用于去重校验 |
| `ext` | String | 文件后缀名 |
| `upload_time` | Long | 上传时间（毫秒时间戳） |

> 图片不做软删除；服务端 DELETE 接口直接物理删除文件及记录。

### 数据库索引

| 表 | 索引 | 用途 |
|------|------|------|
| `bill` | `(book_id, updated_at)` | 增量拉取按账本过滤 + 时间排序 |
| `book` | `(updated_at)` | 增量拉取按时间排序 |
| `category` | `(book_id, updated_at)` | 增量拉取按账本过滤 + 时间排序 |
