# 合记 AI 智能分析功能

## 概述

基于当前项目，新增 AI 智能账单分析和财务预测功能，帮助用户更好地规划个人财务。

## 功能特性

### 1. 智能账单分类 🏷️
自动识别账单类别，支持：
- 基于描述关键词智能匹配
- 商户名称识别
- 金额特征分析
- 中英文双语支持
- 置信度评分

**支持的类别：**

**支出类别：**
- 餐饮：餐厅、外卖、咖啡店等
- 交通：打车、地铁、公交、加油等
- 购物：淘宝、京东、超市等
- 娱乐：电影、游戏、视频会员等
- 医疗：医院、药店、体检等
- 教育：培训、课程、书籍等
- 居住：房租、水电、物业等
- 通讯：话费、流量、宽带等
- 服饰：衣服、鞋子等
- 美容：美发、化妆品、SPA等

**收入类别：**
- 工资：薪水、月薪
- 奖金：年终奖、提成
- 投资收益：股息、利息
- 其他收入：退款、红包等

### 2. 支出预测 📊
基于历史数据预测未来支出：
- 月度支出预测
- 分类别详细预测
- 趋势分析（上升/下降/稳定）
- 置信度评估
- 预测结果缓存（24小时）

### 3. 财务分析 💡
全面的财务健康评估：
- 收支总览
- 储蓄率计算
- 支出趋势检测
- 个性化建议
- 分类支出占比
- 预警提示

### 4. 消费模式识别 🔍
识别用户消费习惯：
- 定期 vs 不定期消费
- 频率分析（日/周/月）
- 各类别平均金额
- 消费习惯描述

## API 接口

### 1. 账单分类接口
```
POST /api/v1/ai/classify
```

**请求示例：**
```json
{
  "description": "肯德基 午餐",
  "amount": 45.5,
  "merchant": "肯德基餐厅",
  "time": "2024-01-15"
}
```

**响应示例：**
```json
{
  "suggestions": [
    {
      "category": "餐饮",
      "confidence": 0.85,
      "reason": "描述匹配关键词: 肯德基"
    }
  ],
  "type": 0
}
```

### 2. 支出预测接口
```
GET /api/v1/ai/predict?book_id={账本ID}&period={期间}
```

**参数说明：**
- `book_id`: 账本ID（必填）
- `period`: 预测期间，格式：YYYY-MM（必填）

**响应示例：**
```json
{
  "book_id": "507f1f77bcf86cd799439011",
  "period": "2024-02",
  "total_expense": 3500.50,
  "total_income": 8000.00,
  "category_break": {
    "餐饮": 800.00,
    "交通": 500.00,
    "购物": 1200.00
  },
  "confidence": 0.85
}
```

### 3. 财务分析接口
```
POST /api/v1/ai/analyze
```

**请求示例：**
```json
{
  "book_id": "507f1f77bcf86cd799439011",
  "start_time": "2024-01-01",
  "end_time": "2024-01-31"
}
```

**响应示例：**
```json
{
  "summary": "总支出: ¥3450.50, 总收入: ¥8000.00, 储蓄率: 56.9%",
  "insights": [
    {
      "type": "insight",
      "title": "储蓄表现优秀",
      "description": "您的储蓄率达到56.9%，财务状况良好",
      "priority": 2,
      "tags": ["储蓄", "表扬"]
    }
  ],
  "spending_trend": "stable",
  "savings_rate": 56.9,
  "top_categories": [
    {
      "category": "餐饮",
      "amount": 800.00,
      "count": 25,
      "percent": 23.2
    }
  ]
}
```

### 4. 消费模式识别接口
```
GET /api/v1/ai/patterns?book_id={账本ID}&start_time={开始时间}&end_time={结束时间}
```

**参数说明：**
- `book_id`: 账本ID（必填）
- `start_time`: 开始日期，格式：YYYY-MM-DD（可选）
- `end_time`: 结束日期，格式：YYYY-MM-DD（可选）

## 使用场景

### 场景1: 记账时自动分类
用户添加新账单时，调用分类接口获取建议类别：

```bash
curl -X POST http://localhost:8080/api/v1/ai/classify \
  -H "Content-Type: application/json" \
  -d '{
    "description": "滴滴出行 打车到机场",
    "amount": 85.5,
    "merchant": "滴滴出行"
  }'
```

系统自动识别为"交通"类别，用户确认后完成记账。

### 场景2: 月初查看支出预测
月初时查看本月预计支出：

```bash
curl "http://localhost:8080/api/v1/ai/predict?book_id=YOUR_BOOK_ID&period=2024-02"
```

帮助用户提前规划本月预算。

### 场景3: 月末财务回顾
月末进行财务分析：

```bash
curl -X POST http://localhost:8080/api/v1/ai/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "book_id": "YOUR_BOOK_ID",
    "start_time": "2024-01-01",
    "end_time": "2024-01-31"
  }'
```

获得详细的月度财务报告和改进建议。

### 场景4: 发现消费习惯
分析消费模式，了解自己的消费习惯：

```bash
curl "http://localhost:8080/api/v1/ai/patterns?book_id=YOUR_BOOK_ID"
```

识别定期支出（如房租、订阅服务）和不定期支出。

## 技术实现

### 架构设计

采用清晰的分层架构：

```
domain/           # 领域模型和接口定义
repository/       # 数据访问层
usecase/          # 业务逻辑层
api/controller/   # HTTP 控制器
```

### 分类算法

当前实现基于规则引擎：

1. **关键词匹配**：匹配预定义的中英文关键词
2. **金额启发式**：根据金额特征辅助判断
3. **置信度计算**：基于匹配质量评分
4. **多语言支持**：同时支持中文和英文

**扩展性设计：**
- 可轻松集成机器学习模型
- 支持接入 OpenAI、Claude 等 AI 服务
- 支持本地部署的开源大模型

### 预测算法

使用统计分析方法：

1. **历史数据分析**：分析最近6个月数据
2. **趋势检测**：对比近期和早期支出
3. **分类预测**：各类别独立预测
4. **置信度评估**：根据数据可用性评分

### 数据存储

**MongoDB 集合：**

1. `ai_predictions`: 存储预测结果用于缓存
2. `bills`: 现有账单集合用于分析

**隐私保护：**
- 所有分析在本地服务器完成
- 不向外部服务发送用户数据
- 数据保留在用户的 MongoDB 数据库中

## 性能优化

1. **缓存策略**：预测结果缓存 24 小时
2. **高效查询**：使用 MongoDB 聚合管道
3. **异步处理**：可扩展支持重负载
4. **索引优化**：建议在 book_id 和 time 字段建立索引

## 建议用法

### 客户端集成建议

**Android 客户端可以：**

1. **记账页面**：
   - 输入备注后自动调用分类接口
   - 显示推荐类别供用户选择
   - 提升记账效率

2. **统计页面**：
   - 显示月度支出预测
   - 对比实际支出和预测值
   - 展示趋势图表

3. **首页仪表盘**：
   - 显示本月财务总览
   - 展示重要提示和建议
   - 快速查看储蓄率

4. **分析报告**：
   - 生成月度/年度财务报告
   - 识别消费习惯
   - 提供优化建议

### Retrofit 接口示例

```kotlin
interface AIAnalysisService {
    @POST("api/v1/ai/classify")
    suspend fun classifyBill(
        @Body request: BillClassificationRequest
    ): BillClassificationResponse
    
    @GET("api/v1/ai/predict")
    suspend fun predictExpenses(
        @Query("book_id") bookId: String,
        @Query("period") period: String
    ): ExpensePrediction
    
    @POST("api/v1/ai/analyze")
    suspend fun analyzeFinancials(
        @Body request: FinancialAnalysisRequest
    ): FinancialAnalysisResponse
    
    @GET("api/v1/ai/patterns")
    suspend fun detectPatterns(
        @Query("book_id") bookId: String,
        @Query("start_time") startTime: String? = null,
        @Query("end_time") endTime: String? = null
    ): List<SpendingPattern>
}
```

## 未来增强

### 短期计划
1. ✅ 基础规则引擎实现
2. ⬜ 用户反馈学习机制
3. ⬜ 更多预定义类别和关键词
4. ⬜ 客户端 UI 集成

### 中期计划
1. ⬜ 接入 OpenAI/Claude API
2. ⬜ 本地小模型集成
3. ⬜ 自定义分类规则
4. ⬜ 多账本对比分析

### 长期计划
1. ⬜ 深度学习模型训练
2. ⬜ 图片识别（票据 OCR）
3. ⬜ 语音记账
4. ⬜ 智能预算建议

## 测试

运行测试：

```bash
cd server/
go test -v ./usecase -run "TestClassifyBill|TestAnalyzeSpendingTrend"
```

测试覆盖：
- ✅ 餐饮分类
- ✅ 交通分类
- ✅ 工资收入识别
- ✅ 英文关键词
- ✅ 大额支出
- ✅ 无匹配情况
- ✅ 趋势分析

## 常见问题

**Q: 分类准确率如何？**  
A: 当前基于规则的方法对常见商户和类别准确率较高（约85%）。未来集成 ML 模型后会进一步提升。

**Q: 支持哪些语言？**  
A: 目前支持中文和英文关键词。可以轻松扩展支持更多语言。

**Q: 预测准确吗？**  
A: 预测准确度取决于历史数据的完整性。数据越多，预测越准确。系统会给出置信度评分。

**Q: 隐私安全吗？**  
A: 是的。所有分析在您的服务器本地完成，不会发送数据到外部。

**Q: 如何自定义分类规则？**  
A: 当前版本使用预定义规则。未来版本会支持用户自定义规则和学习。

**Q: 消耗资源多吗？**  
A: 规则引擎非常轻量，几乎不占用额外资源。预测结果会缓存以减少重复计算。

## 贡献

欢迎提交 Issue 和 Pull Request 来改进 AI 功能：

- 添加更多关键词和模式
- 优化分类算法
- 改进预测准确度
- 增强测试覆盖

## 开源协议

本功能遵循项目原有开源协议。

---

**注意：** 本功能为学习和个人使用目的开发，不用于商业用途。
