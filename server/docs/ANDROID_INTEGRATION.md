# Android Client Integration Guide for AI Features

This guide shows how to integrate the AI analysis features into the Android client.

## Overview

The AI features can enhance the Android app with:
1. Auto-complete category suggestions when adding bills
2. Monthly expense predictions in statistics view
3. Financial health insights on dashboard
4. Spending pattern analysis in reports

## Step 1: Define Data Models

Create Kotlin data classes for API requests and responses:

```kotlin
// File: app/src/main/java/com/hao/heji/data/ai/AIModels.kt

package com.hao.heji.data.ai

data class BillClassificationRequest(
    val description: String,
    val amount: Double,
    val merchant: String? = null,
    val time: String? = null
)

data class CategorySuggestion(
    val category: String,
    val confidence: Double,
    val reason: String
)

data class BillClassificationResponse(
    val suggestions: List<CategorySuggestion>,
    val type: Int // 0: expense, 1: income
)

data class ExpensePrediction(
    val book_id: String,
    val period: String,
    val total_expense: Double,
    val total_income: Double,
    val category_break: Map<String, Double>,
    val confidence: Double,
    val created_at: Long
)

data class FinancialInsight(
    val type: String, // "warning", "suggestion", "insight"
    val title: String,
    val description: String,
    val priority: Int,
    val tags: List<String>
)

data class CategorySpending(
    val category: String,
    val amount: Double,
    val count: Int,
    val percent: Double
)

data class FinancialAnalysisRequest(
    val book_id: String,
    val start_time: String,
    val end_time: String
)

data class FinancialAnalysisResponse(
    val summary: String,
    val insights: List<FinancialInsight>,
    val spending_trend: String,
    val savings_rate: Double,
    val top_categories: List<CategorySpending>,
    val predictions: ExpensePrediction?
)

data class SpendingPattern(
    val pattern: String,
    val frequency: String,
    val avg_amount: Double,
    val category: String,
    val description: String
)
```

## Step 2: Create Retrofit Service

Add the AI service interface:

```kotlin
// File: app/src/main/java/com/hao/heji/data/api/AIAnalysisService.kt

package com.hao.heji.data.api

import com.hao.heji.data.ai.*
import retrofit2.http.*

interface AIAnalysisService {
    
    @POST("ai/classify")
    suspend fun classifyBill(
        @Body request: BillClassificationRequest
    ): BillClassificationResponse
    
    @GET("ai/predict")
    suspend fun predictExpenses(
        @Query("book_id") bookId: String,
        @Query("period") period: String
    ): ExpensePrediction
    
    @POST("ai/analyze")
    suspend fun analyzeFinancials(
        @Body request: FinancialAnalysisRequest
    ): FinancialAnalysisResponse
    
    @GET("ai/patterns")
    suspend fun detectPatterns(
        @Query("book_id") bookId: String,
        @Query("start_time") startTime: String? = null,
        @Query("end_time") endTime: String? = null
    ): List<SpendingPattern>
}
```

## Step 3: Create Repository

Implement a repository for AI features:

```kotlin
// File: app/src/main/java/com/hao/heji/data/repository/AIAnalysisRepository.kt

package com.hao.heji.data.repository

import com.hao.heji.data.ai.*
import com.hao.heji.data.api.AIAnalysisService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIAnalysisRepository @Inject constructor(
    private val aiService: AIAnalysisService
) {
    
    suspend fun classifyBill(
        description: String,
        amount: Double,
        merchant: String? = null
    ): Result<BillClassificationResponse> = withContext(Dispatchers.IO) {
        try {
            val request = BillClassificationRequest(
                description = description,
                amount = amount,
                merchant = merchant
            )
            val response = aiService.classifyBill(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun predictExpenses(
        bookId: String,
        period: String
    ): Result<ExpensePrediction> = withContext(Dispatchers.IO) {
        try {
            val prediction = aiService.predictExpenses(bookId, period)
            Result.success(prediction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun analyzeFinancials(
        bookId: String,
        startTime: String,
        endTime: String
    ): Result<FinancialAnalysisResponse> = withContext(Dispatchers.IO) {
        try {
            val request = FinancialAnalysisRequest(
                book_id = bookId,
                start_time = startTime,
                end_time = endTime
            )
            val analysis = aiService.analyzeFinancials(request)
            Result.success(analysis)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun detectPatterns(
        bookId: String,
        startTime: String? = null,
        endTime: String? = null
    ): Result<List<SpendingPattern>> = withContext(Dispatchers.IO) {
        try {
            val patterns = aiService.detectPatterns(bookId, startTime, endTime)
            Result.success(patterns)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Step 4: Usage in ViewModels

### Example 1: Auto-classify when adding a bill

```kotlin
// In your AddBillViewModel

private val aiRepository: AIAnalysisRepository = // inject

fun onDescriptionChanged(description: String, amount: Double) {
    if (description.length >= 3) { // Start suggesting after 3 characters
        viewModelScope.launch {
            aiRepository.classifyBill(description, amount).fold(
                onSuccess = { response ->
                    // Update UI with suggestions
                    _categorySuggestions.value = response.suggestions
                    _billType.value = response.type
                },
                onFailure = { error ->
                    // Handle error silently or show a subtle message
                    Log.e("AI", "Classification failed: ${error.message}")
                }
            )
        }
    }
}
```

### Example 2: Show monthly predictions

```kotlin
// In your StatisticsViewModel

fun loadMonthlyPrediction(bookId: String, yearMonth: String) {
    viewModelScope.launch {
        _predictionLoading.value = true
        
        aiRepository.predictExpenses(bookId, yearMonth).fold(
            onSuccess = { prediction ->
                _monthlyPrediction.value = prediction
                _predictionLoading.value = false
            },
            onFailure = { error ->
                _predictionError.value = error.message
                _predictionLoading.value = false
            }
        )
    }
}
```

### Example 3: Show financial analysis

```kotlin
// In your DashboardViewModel

fun loadFinancialAnalysis(bookId: String) {
    viewModelScope.launch {
        val startTime = // first day of month
        val endTime = // today
        
        aiRepository.analyzeFinancials(bookId, startTime, endTime).fold(
            onSuccess = { analysis ->
                _financialInsights.value = analysis.insights
                _savingsRate.value = analysis.savings_rate
                _spendingTrend.value = analysis.spending_trend
            },
            onFailure = { error ->
                Log.e("AI", "Analysis failed: ${error.message}")
            }
        )
    }
}
```

## Step 5: UI Components

### Category Suggestion Chip

```kotlin
// In your add bill screen

@Composable
fun CategorySuggestions(
    suggestions: List<CategorySuggestion>,
    onSuggestionClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(suggestions) { suggestion ->
            SuggestionChip(
                onClick = { onSuggestionClick(suggestion.category) },
                label = { 
                    Text(
                        text = suggestion.category,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    }
}
```

### Financial Insight Card

```kotlin
@Composable
fun FinancialInsightCard(insight: FinancialInsight) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (insight.type) {
                "warning" -> MaterialTheme.colorScheme.errorContainer
                "suggestion" -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = when (insight.type) {
                        "warning" -> Icons.Default.Warning
                        "suggestion" -> Icons.Default.TipsAndUpdates
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = insight.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
```

### Prediction vs Actual Chart

```kotlin
@Composable
fun PredictionVsActualChart(
    predicted: Double,
    actual: Double,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "本月支出预测",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("预测", style = MaterialTheme.typography.labelMedium)
                Text(
                    "¥${String.format("%.2f", predicted)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("实际", style = MaterialTheme.typography.labelMedium)
                Text(
                    "¥${String.format("%.2f", actual)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        
        val difference = actual - predicted
        val percentage = if (predicted > 0) (difference / predicted * 100) else 0.0
        
        Text(
            text = when {
                difference > 0 -> "超支 ¥${String.format("%.2f", difference)} (+${String.format("%.1f", percentage)}%)"
                difference < 0 -> "节省 ¥${String.format("%.2f", -difference)} (${String.format("%.1f", -percentage)}%)"
                else -> "与预测一致"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                difference > 0 -> MaterialTheme.colorScheme.error
                difference < 0 -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
```

## Step 6: Testing

Test the integration:

```kotlin
// Unit test example

@Test
fun `classifyBill returns suggestions`() = runTest {
    val repository = AIAnalysisRepository(mockService)
    
    val result = repository.classifyBill(
        description = "肯德基午餐",
        amount = 45.5,
        merchant = "肯德基"
    )
    
    assertTrue(result.isSuccess)
    val response = result.getOrNull()
    assertNotNull(response)
    assertTrue(response!!.suggestions.isNotEmpty())
}
```

## Step 7: User Experience Considerations

1. **Auto-complete delay**: Add a debounce (300-500ms) before calling classification API
2. **Loading states**: Show subtle loading indicators
3. **Error handling**: Fail silently or show non-intrusive error messages
4. **Offline support**: Cache last predictions for offline viewing
5. **User override**: Always allow users to manually select category
6. **Feedback loop**: Let users report incorrect suggestions (future feature)

## Step 8: Performance Tips

1. **Debounce**: Use debounce for real-time classification
2. **Cache**: Cache prediction results for 24 hours
3. **Background loading**: Load insights in background
4. **Pagination**: For large insight lists, use pagination
5. **Lazy loading**: Load AI features only when needed

## Example: Complete Add Bill Flow

```kotlin
@Composable
fun AddBillScreen(viewModel: AddBillViewModel) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val suggestions by viewModel.categorySuggestions.collectAsState()
    
    Column {
        // Description field
        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
                val amountDouble = amount.toDoubleOrNull() ?: 0.0
                viewModel.onDescriptionChanged(it, amountDouble)
            },
            label = { Text("描述") }
        )
        
        // Amount field
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("金额") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
        
        // AI Suggestions
        if (suggestions.isNotEmpty()) {
            Text(
                "AI 建议分类:",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 16.dp, start = 16.dp)
            )
            CategorySuggestions(
                suggestions = suggestions,
                onSuggestionClick = { category ->
                    viewModel.selectCategory(category)
                }
            )
        }
        
        // Manual category selection
        CategoryPicker(
            selectedCategory = viewModel.selectedCategory.value,
            onCategorySelect = { viewModel.selectCategory(it) }
        )
        
        // Save button
        Button(
            onClick = { viewModel.saveBill() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("保存")
        }
    }
}
```

## Configuration

Add to your `gradle.properties` for different flavors:

```properties
# Local development
AI_ENABLED=true
AI_BASE_URL=http://10.0.2.2:8080/api/v1/

# Production
AI_ENABLED=true
AI_BASE_URL=https://your-server.com/api/v1/
```

## Conclusion

This integration guide provides a complete example of how to integrate AI features into the Android client. The implementation is:

- **Non-intrusive**: Works alongside existing features
- **User-friendly**: Provides helpful suggestions without forcing them
- **Performant**: Uses debouncing and caching
- **Testable**: Well-structured for unit testing
- **Extensible**: Easy to add more AI features in the future

For more details on the API, see:
- [API Documentation](./AI_ANALYSIS_API.md)
- [Feature Guide (中文)](./AI_FEATURES_CN.md)
