# 合記 (Heji) - Android 客户端

多人共享记账应用，支持实时同步、离线使用、多账本管理。

## 截图

| 首页 | 记账 | 统计 |
|:---:|:---:|:---:|
| ![首页](docs/img/home.png) | ![记账](docs/img/save.png) | ![统计](docs/img/total1.png) |

| 日历 | 账本列表 | 设置 |
|:---:|:---:|:---:|
| ![日历](docs/img/timeview.png) | ![账本](docs/img/booklist.png) | ![设置](docs/img/setting.png) |

## 功能特性

- **记账管理** — 收入/支出记录，支持金额、备注、分类、日期、票据图片
- **多账本** — 创建多个账本，支持多人共享协作
- **自定义分类** — 按账本自定义收入/支出分类
- **统计报表** — 按分类、月度、年度统计收支，图表可视化 (MPAndroidChart)
- **日历视图** — 按日历查看每日账单
- **实时同步** — 基于 WebSocket 的实时数据同步
- **离线优先** — 无网络下正常使用，连网后自动同步
- **数据导入** — 支持支付宝、微信、钱迹、Excel 格式导入
- **数据导出** — 支持 Excel、CSV 格式导出
- **ETC 查询** — 高速通行费查询
- **用户认证** — 注册/登录，JWT 认证

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin 2.3 |
| 架构 | Single Activity + Fragment MVI |
| 依赖注入 | Koin 4.1 |
| 数据库 | Room 2.8 |
| 网络请求 | Retrofit 3.0 + OkHttp 5.3 |
| 实时同步 | OkHttp WebSocket |
| 序列化 | Kotlinx Serialization |
| 导航 | Navigation Component 2.9 |
| 图片加载 | Glide 4.15 |
| 图表 | MPAndroidChart 3.1 |
| 数据存储 | MMKV |
| 监控 | Sentry |

## 环境要求

- Android Studio Ladybug 或更高版本
- JDK 17
- Android SDK: minSdk 24 (Android 7.0+), targetSdk 35, compileSdk 36

## 构建 & 运行

```bash
# 构建调试版
./gradlew assembleDebug

# 安装调试版到设备
./gradlew installDebug

# 构建发布版 (包含 local 和 cloud 两个变体)
./gradlew assembleRelease

# 运行单元测试
./gradlew test

# 运行设备测试
./gradlew connectedAndroidTest
```

## 构建变体 (Product Flavors)

| 变体 | 包名 | 应用名 | 服务器 |
|------|------|--------|--------|
| **local** | `com.hao.heji_test` | 合記开发版 | `http://192.168.8.68:8888` |
| **cloud** | `com.hao.heji` | 合記 | `https://dev.hao88.cloud` |

## 项目结构

```
app/src/main/java/com/hao/heji/
├── data/
│   └── db/           # Room 数据库实体 (Bill, Book, Category, Image)
├── di/               # Koin 依赖注入模块
├── sync/             # WebSocket 同步 (SyncService, WebSocketClient)
├── network/          # Retrofit API 定义
├── ui/               # 各功能页面 Fragment + ViewModel
│   ├── bill/         # 账单列表、创建
│   ├── book/         # 账本管理
│   ├── category/     # 分类管理
│   ├── report/       # 统计报表
│   ├── calendar/     # 日历视图
│   ├── setting/      # 设置、导出
│   └── user/         # 用户信息
└── App.kt            # Application 入口
```

## 数据同步

采用**本地优先**策略：

1. 数据优先写入本地 Room 数据库
2. 通过 WebSocket 在后台与服务端实时同步
3. 网络断开时离线使用，恢复后自动重连同步
4. 基于服务端时间戳的冲突解决

详见 [同步文档](docs/sync.md) | [数据说明](docs/data.md)
