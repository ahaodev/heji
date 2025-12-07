# AI Analysis & Financial Prediction API

This document describes the AI-powered bill analysis and financial prediction features added to the Heji server.

## Overview

The AI Analysis feature provides intelligent bill classification, expense prediction, financial insights, and spending pattern detection to help users better understand and manage their finances.

## Features

### 1. Bill Classification
Automatically classifies bills into appropriate categories based on:
- Bill description/remark
- Merchant name
- Transaction amount
- Transaction patterns

### 2. Expense Prediction
Predicts future expenses based on historical data:
- Monthly expense forecasting
- Category-specific predictions
- Trend analysis
- Confidence scoring

### 3. Financial Analysis
Comprehensive financial health assessment:
- Income vs. expense analysis
- Savings rate calculation
- Spending trend detection
- Personalized insights and recommendations
- Category breakdown

### 4. Pattern Detection
Identifies recurring spending patterns:
- Regular vs. irregular expenses
- Frequency analysis (daily, weekly, monthly)
- Category-specific patterns
- Average amounts per pattern

## API Endpoints

### 1. POST /api/v1/ai/classify
Classify a bill using AI to suggest appropriate categories.

**Request Body:**
```json
{
  "description": "肯德基 午餐",
  "amount": 45.5,
  "merchant": "肯德基餐厅",
  "time": "2024-01-15"
}
```

**Response:**
```json
{
  "suggestions": [
    {
      "category": "餐饮",
      "confidence": 0.85,
      "reason": "描述匹配关键词: 肯德基"
    },
    {
      "category": "其他",
      "confidence": 0.5,
      "reason": "备选类别"
    }
  ],
  "type": 0
}
```

**Supported Categories:**
- **Income (type=1):** 工资, 奖金, 投资收益, 其他收入
- **Expense (type=0):** 餐饮, 交通, 购物, 娱乐, 医疗, 教育, 居住, 通讯, 服饰, 美容

### 2. GET /api/v1/ai/predict
Predict future expenses for a specific period.

**Query Parameters:**
- `book_id` (required): The book ID
- `period` (required): Prediction period (format: YYYY-MM)

**Example:**
```
GET /api/v1/ai/predict?book_id=507f1f77bcf86cd799439011&period=2024-02
```

**Response:**
```json
{
  "_id": "507f1f77bcf86cd799439012",
  "book_id": "507f1f77bcf86cd799439011",
  "user_id": "user123",
  "period": "2024-02",
  "total_expense": 3500.50,
  "total_income": 8000.00,
  "category_break": {
    "餐饮": 800.00,
    "交通": 500.00,
    "购物": 1200.00,
    "居住": 1000.50
  },
  "confidence": 0.85,
  "created_at": 1704067200
}
```

### 3. POST /api/v1/ai/analyze
Perform comprehensive financial analysis.

**Request Body:**
```json
{
  "book_id": "507f1f77bcf86cd799439011",
  "start_time": "2024-01-01",
  "end_time": "2024-01-31"
}
```

**Response:**
```json
{
  "summary": "总支出: ¥3450.50, 总收入: ¥8000.00, 储蓄率: 56.9%, 支出趋势: 保持稳定。建议: 保持良好的记账习惯，定期审查支出类别，设置月度预算目标。",
  "insights": [
    {
      "type": "insight",
      "title": "储蓄表现优秀",
      "description": "您的储蓄率达到56.9%，财务状况良好",
      "priority": 2,
      "tags": ["储蓄", "表扬"]
    },
    {
      "type": "suggestion",
      "title": "优化支出结构",
      "description": "建议关注餐饮支出，占比较高",
      "priority": 3,
      "tags": ["类别分析", "建议"]
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
    },
    {
      "category": "交通",
      "amount": 500.00,
      "count": 18,
      "percent": 14.5
    }
  ],
  "predictions": {
    "period": "2024-02",
    "total_expense": 3500.00,
    "total_income": 8000.00,
    "confidence": 0.85
  }
}
```

### 4. GET /api/v1/ai/patterns
Detect spending patterns and habits.

**Query Parameters:**
- `book_id` (required): The book ID
- `start_time` (optional): Start date (YYYY-MM-DD)
- `end_time` (optional): End date (YYYY-MM-DD)

**Example:**
```
GET /api/v1/ai/patterns?book_id=507f1f77bcf86cd799439011&start_time=2024-01-01&end_time=2024-01-31
```

**Response:**
```json
[
  {
    "pattern": "regular",
    "frequency": "monthly",
    "avg_amount": 1000.50,
    "category": "居住",
    "description": "每月定期在居住类别消费，平均金额1000.50元"
  },
  {
    "pattern": "irregular",
    "frequency": "irregular",
    "avg_amount": 245.80,
    "category": "娱乐",
    "description": "娱乐类别的支出"
  }
]
```

## Implementation Details

### Architecture

The AI analysis feature follows the clean architecture pattern:

```
domain/ai_analysis.go          # Domain models and interfaces
repository/ai_analysis_repository.go  # Data access layer
usecase/ai_analysis_usecase.go        # Business logic
api/controller/ai_analysis_controller.go  # HTTP handlers
```

### Classification Algorithm

The current implementation uses a rule-based classification system:

1. **Keyword Matching**: Matches bill descriptions against predefined patterns
2. **Amount Heuristics**: Uses transaction amounts to refine classifications
3. **Multi-language Support**: Supports both Chinese and English keywords
4. **Confidence Scoring**: Assigns confidence levels based on match quality

### Prediction Algorithm

Expense prediction uses statistical analysis:

1. **Historical Analysis**: Analyzes last 6 months of data
2. **Trend Detection**: Compares recent vs. older spending patterns
3. **Category Breakdown**: Predicts per-category expenses
4. **Confidence Calculation**: Based on data availability and consistency

### Future Enhancements

The current implementation is designed to be extensible:

1. **Machine Learning Integration**: Can be enhanced with ML models
2. **External AI APIs**: Ready to integrate OpenAI, Claude, or local LLMs
3. **User Feedback Loop**: Learn from user corrections
4. **Advanced Pattern Recognition**: Deep learning for complex patterns

## Usage Examples

### Example 1: Auto-classify a new bill

When adding a bill, call the classification endpoint to get category suggestions:

```bash
curl -X POST http://localhost:8080/api/v1/ai/classify \
  -H "Content-Type: application/json" \
  -d '{
    "description": "滴滴出行 打车到机场",
    "amount": 85.5,
    "merchant": "滴滴出行"
  }'
```

### Example 2: Get monthly predictions

Before the month starts, check predicted expenses:

```bash
curl "http://localhost:8080/api/v1/ai/predict?book_id=507f1f77bcf86cd799439011&period=2024-02"
```

### Example 3: Monthly financial review

At the end of the month, analyze spending:

```bash
curl -X POST http://localhost:8080/api/v1/ai/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "book_id": "507f1f77bcf86cd799439011",
    "start_time": "2024-01-01",
    "end_time": "2024-01-31"
  }'
```

## Error Handling

All endpoints return appropriate HTTP status codes:

- `200 OK`: Success
- `400 Bad Request`: Invalid input parameters
- `500 Internal Server Error`: Server-side error

Error response format:
```json
{
  "message": "Error description"
}
```

## Data Storage

### Collections

1. **ai_predictions**: Stores prediction results for caching
   - Predictions are cached for 24 hours
   - Indexed by book_id and period

2. **bills**: Existing bills collection used for analysis

### Privacy & Security

- All analysis is performed locally on the server
- No data is sent to external AI services (in current implementation)
- User data remains in the MongoDB database
- Future ML integration will follow privacy-first design

## Performance Considerations

1. **Caching**: Predictions are cached to avoid redundant calculations
2. **Efficient Queries**: MongoDB aggregation pipelines for statistics
3. **Asynchronous Processing**: Can be enhanced for heavy workloads
4. **Rate Limiting**: Consider adding for production use

## Configuration

Currently, no additional configuration is required. The AI features work out of the box with the existing MongoDB setup.

For future ML integration, add to `config.yml`:
```yaml
ai:
  provider: "local"  # or "openai", "custom"
  model: ""
  api_key: ""
```

## Testing

Test the endpoints manually:

```bash
# 1. Classification
curl -X POST http://localhost:8080/api/v1/ai/classify \
  -H "Content-Type: application/json" \
  -d '{"description": "美团外卖", "amount": 35}'

# 2. Prediction (requires existing book_id)
curl "http://localhost:8080/api/v1/ai/predict?book_id=YOUR_BOOK_ID&period=2024-02"

# 3. Analysis (requires existing book_id and bills)
curl -X POST http://localhost:8080/api/v1/ai/analyze \
  -H "Content-Type: application/json" \
  -d '{"book_id": "YOUR_BOOK_ID", "start_time": "2024-01-01", "end_time": "2024-01-31"}'

# 4. Patterns
curl "http://localhost:8080/api/v1/ai/patterns?book_id=YOUR_BOOK_ID"
```

## Client Integration

To integrate with the Android client, add API calls in the appropriate data layer:

1. Create data models matching the response DTOs
2. Add Retrofit service interfaces for the AI endpoints
3. Implement UI for displaying:
   - Category suggestions when adding bills
   - Monthly predictions in statistics view
   - Financial insights in dashboard
   - Pattern analysis in reports

Example Retrofit interface:
```kotlin
interface AIAnalysisService {
    @POST("api/v1/ai/classify")
    suspend fun classifyBill(@Body request: BillClassificationRequest): BillClassificationResponse
    
    @GET("api/v1/ai/predict")
    suspend fun predictExpenses(
        @Query("book_id") bookId: String,
        @Query("period") period: String
    ): ExpensePrediction
    
    @POST("api/v1/ai/analyze")
    suspend fun analyzeFinancials(@Body request: FinancialAnalysisRequest): FinancialAnalysisResponse
    
    @GET("api/v1/ai/patterns")
    suspend fun detectPatterns(@Query("book_id") bookId: String): List<SpendingPattern>
}
```
