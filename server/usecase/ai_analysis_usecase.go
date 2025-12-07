package usecase

import (
	"context"
	"fmt"
	"heji-server/domain"
	"sort"
	"strings"
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type aiAnalysisUseCase struct {
	repository domain.AIAnalysisRepository
}

// NewAIAnalysisUseCase creates a new AI analysis use case
func NewAIAnalysisUseCase(repo domain.AIAnalysisRepository) domain.AIAnalysisUseCase {
	return &aiAnalysisUseCase{
		repository: repo,
	}
}

// ClassifyBill performs AI-based bill classification
func (uc *aiAnalysisUseCase) ClassifyBill(ctx context.Context, req *domain.BillClassificationRequest) (*domain.BillClassificationResponse, error) {
	// Rule-based classification system
	suggestions := []domain.CategorySuggestion{}
	billType := 0 // Default to expense

	// Normalize description for matching
	desc := strings.ToLower(req.Description)
	merchant := strings.ToLower(req.Merchant)
	combined := desc + " " + merchant

	// Income detection patterns
	incomePatterns := map[string][]string{
		"工资": {"工资", "薪水", "salary", "工资收入", "月薪"},
		"奖金": {"奖金", "年终奖", "bonus", "提成"},
		"投资收益": {"股息", "分红", "利息", "dividend", "interest"},
		"其他收入": {"退款", "红包", "礼金", "refund"},
	}

	// Expense category patterns with Chinese and English support
	expensePatterns := map[string][]string{
		"餐饮": {"餐饮", "饭店", "美食", "食堂", "外卖", "restaurant", "food", "cafe", "coffee", "肯德基", "麦当劳", "星巴克"},
		"交通": {"交通", "打车", "滴滴", "地铁", "公交", "taxi", "uber", "transport", "停车", "加油"},
		"购物": {"淘宝", "京东", "天猫", "超市", "商场", "shopping", "store", "mall", "拼多多"},
		"娱乐": {"电影", "ktv", "游戏", "视频", "会员", "entertainment", "movie", "netflix", "spotify"},
		"医疗": {"医院", "药店", "体检", "hospital", "pharmacy", "医疗", "health"},
		"教育": {"培训", "课程", "学费", "书籍", "education", "course", "book", "tuition"},
		"居住": {"房租", "水电", "物业", "rent", "utility", "住房", "房贷"},
		"通讯": {"话费", "流量", "宽带", "mobile", "internet", "telecom"},
		"服饰": {"衣服", "鞋子", "服装", "clothing", "shoes", "fashion"},
		"美容": {"美容", "美发", "化妆品", "beauty", "cosmetics", "spa"},
	}

	// Check for income patterns
	for category, patterns := range incomePatterns {
		for _, pattern := range patterns {
			if strings.Contains(combined, pattern) {
				suggestions = append(suggestions, domain.CategorySuggestion{
					Category:   category,
					Confidence: 0.85,
					Reason:     fmt.Sprintf("描述中包含收入关键词: %s", pattern),
				})
				billType = 1 // Income
				break
			}
		}
	}

	// If not income, check for expense categories
	if billType == 0 {
		matchedCategories := make(map[string]float64)
		matchedReasons := make(map[string]string)

		for category, patterns := range expensePatterns {
			matchCount := 0
			var matchedPattern string
			for _, pattern := range patterns {
				if strings.Contains(combined, pattern) {
					matchCount++
					matchedPattern = pattern
					break
				}
			}

			if matchCount > 0 {
				confidence := 0.7 + float64(matchCount)*0.1
				if confidence > 0.95 {
					confidence = 0.95
				}
				matchedCategories[category] = confidence
				matchedReasons[category] = fmt.Sprintf("描述匹配关键词: %s", matchedPattern)
			}
		}

		// Amount-based heuristics
		if req.Amount > 0 {
			if req.Amount > 5000 {
				if _, exists := matchedCategories["居住"]; !exists {
					matchedCategories["居住"] = 0.6
					matchedReasons["居住"] = "大额支出通常与住房相关"
				}
			} else if req.Amount < 50 {
				if _, exists := matchedCategories["餐饮"]; !exists {
					matchedCategories["餐饮"] = 0.5
					matchedReasons["餐饮"] = "小额支出常见于日常餐饮"
				}
			}
		}

		// Convert to suggestions and sort by confidence
		for category, confidence := range matchedCategories {
			suggestions = append(suggestions, domain.CategorySuggestion{
				Category:   category,
				Confidence: confidence,
				Reason:     matchedReasons[category],
			})
		}

		// Sort by confidence
		sort.Slice(suggestions, func(i, j int) bool {
			return suggestions[i].Confidence > suggestions[j].Confidence
		})

		// Keep top 3 suggestions
		if len(suggestions) > 3 {
			suggestions = suggestions[:3]
		}

		// If no matches, provide default categories
		if len(suggestions) == 0 {
			suggestions = append(suggestions, domain.CategorySuggestion{
				Category:   "其他",
				Confidence: 0.5,
				Reason:     "未找到明确匹配的类别",
			})
		}
	}

	return &domain.BillClassificationResponse{
		Suggestions: suggestions,
		Type:        billType,
	}, nil
}

// PredictExpenses predicts future expenses based on historical data
func (uc *aiAnalysisUseCase) PredictExpenses(ctx context.Context, bookID string, period string) (*domain.ExpensePrediction, error) {
	// Try to get cached prediction
	cached, err := uc.repository.GetLatestPrediction(ctx, bookID, period)
	if err == nil && cached != nil {
		// If prediction is less than 24 hours old, return it
		if time.Now().Unix()-cached.CreatedAt < 86400 {
			return cached, nil
		}
	}

	// Parse period (e.g., "2024-01" for monthly prediction)
	var startTime, endTime string
	
	// Get historical data for the past 6 months for prediction
	now := time.Now()
	endTime = now.Format("2006-01-02")
	startTime = now.AddDate(0, -6, 0).Format("2006-01-02")

	bills, err := uc.repository.GetBillsForAnalysis(ctx, bookID, startTime, endTime)
	if err != nil {
		return nil, err
	}

	// Calculate statistics
	var totalExpense, totalIncome float64
	categoryBreak := make(map[string]float64)
	monthlyExpenses := make(map[string]float64)

	for _, bill := range bills {
		amount := parseAmount(bill.Money)
		
		if bill.Type == 0 { // Expense
			totalExpense += amount
			categoryBreak[bill.Category] += amount
			
			// Track monthly expenses
			month := bill.Time[:7] // Extract YYYY-MM
			monthlyExpenses[month] += amount
		} else { // Income
			totalIncome += amount
		}
	}

	// Calculate average monthly expense
	monthCount := len(monthlyExpenses)
	if monthCount == 0 {
		monthCount = 1
	}
	avgMonthlyExpense := totalExpense / float64(monthCount)

	// Simple trend analysis
	var trendMultiplier float64 = 1.0
	if len(monthlyExpenses) >= 3 {
		// Compare recent months to older months
		var recentSum, olderSum float64
		var recentCount, olderCount int

		sortedMonths := make([]string, 0, len(monthlyExpenses))
		for month := range monthlyExpenses {
			sortedMonths = append(sortedMonths, month)
		}
		sort.Strings(sortedMonths)

		midPoint := len(sortedMonths) / 2
		for i, month := range sortedMonths {
			if i >= midPoint {
				recentSum += monthlyExpenses[month]
				recentCount++
			} else {
				olderSum += monthlyExpenses[month]
				olderCount++
			}
		}

		if olderCount > 0 && recentCount > 0 {
			recentAvg := recentSum / float64(recentCount)
			olderAvg := olderSum / float64(olderCount)
			
			if olderAvg > 0 {
				trend := recentAvg / olderAvg
				// Apply moderate trend adjustment
				if trend > 1.2 {
					trendMultiplier = 1.1 // Increasing trend
				} else if trend < 0.8 {
					trendMultiplier = 0.9 // Decreasing trend
				}
			}
		}
	}

	// Predict next month's expenses
	predictedExpense := avgMonthlyExpense * trendMultiplier
	predictedIncome := totalIncome / float64(monthCount)

	// Predict category breakdown
	predictedCategoryBreak := make(map[string]float64)
	for category, amount := range categoryBreak {
		avgCategoryAmount := amount / float64(monthCount)
		predictedCategoryBreak[category] = avgCategoryAmount * trendMultiplier
	}

	// Calculate confidence based on data availability
	confidence := 0.5
	if monthCount >= 3 {
		confidence = 0.7
	}
	if monthCount >= 6 {
		confidence = 0.85
	}

	prediction := &domain.ExpensePrediction{
		BookID:        mustParseObjectID(bookID),
		Period:        period,
		TotalExpense:  predictedExpense,
		TotalIncome:   predictedIncome,
		CategoryBreak: predictedCategoryBreak,
		Confidence:    confidence,
	}

	// Save prediction
	err = uc.repository.SavePrediction(ctx, prediction)
	if err != nil {
		// Log error but still return prediction
		fmt.Printf("Failed to save prediction: %v\n", err)
	}

	return prediction, nil
}

// AnalyzeFinancials provides comprehensive financial analysis
func (uc *aiAnalysisUseCase) AnalyzeFinancials(ctx context.Context, req *domain.FinancialAnalysisRequest) (*domain.FinancialAnalysisResponse, error) {
	bills, err := uc.repository.GetBillsForAnalysis(ctx, req.BookID, req.StartTime, req.EndTime)
	if err != nil {
		return nil, err
	}

	if len(bills) == 0 {
		return &domain.FinancialAnalysisResponse{
			Summary:       "暂无账单数据进行分析",
			Insights:      []domain.FinancialInsight{},
			SpendingTrend: "stable",
			SavingsRate:   0,
			TopCategories: []domain.CategorySpending{},
		}, nil
	}

	// Calculate totals and category breakdown
	var totalExpense, totalIncome float64
	categoryStats := make(map[string]*domain.CategorySpending)
	monthlyExpenses := make(map[string]float64)

	for _, bill := range bills {
		amount := parseAmount(bill.Money)
		
		if bill.Type == 0 { // Expense
			totalExpense += amount
			
			if _, exists := categoryStats[bill.Category]; !exists {
				categoryStats[bill.Category] = &domain.CategorySpending{
					Category: bill.Category,
				}
			}
			categoryStats[bill.Category].Amount += amount
			categoryStats[bill.Category].Count++
			
			month := bill.Time[:7]
			monthlyExpenses[month] += amount
		} else { // Income
			totalIncome += amount
		}
	}

	// Calculate percentages
	for _, stat := range categoryStats {
		if totalExpense > 0 {
			stat.Percent = (stat.Amount / totalExpense) * 100
		}
	}

	// Convert to sorted slice
	topCategories := make([]domain.CategorySpending, 0, len(categoryStats))
	for _, stat := range categoryStats {
		topCategories = append(topCategories, *stat)
	}
	sort.Slice(topCategories, func(i, j int) bool {
		return topCategories[i].Amount > topCategories[j].Amount
	})

	// Keep top 5
	if len(topCategories) > 5 {
		topCategories = topCategories[:5]
	}

	// Calculate savings rate
	savingsRate := 0.0
	if totalIncome > 0 {
		savingsRate = ((totalIncome - totalExpense) / totalIncome) * 100
	}

	// Analyze spending trend
	spendingTrend := analyzeSpendingTrend(monthlyExpenses)

	// Generate insights
	insights := generateFinancialInsights(totalExpense, totalIncome, savingsRate, topCategories, spendingTrend)

	// Generate summary
	summary := generateFinancialSummary(totalExpense, totalIncome, savingsRate, spendingTrend)

	// Get predictions for next month
	nextMonth := time.Now().AddDate(0, 1, 0).Format("2006-01")
	predictions, _ := uc.PredictExpenses(ctx, req.BookID, nextMonth)

	return &domain.FinancialAnalysisResponse{
		Summary:       summary,
		Insights:      insights,
		SpendingTrend: spendingTrend,
		SavingsRate:   savingsRate,
		TopCategories: topCategories,
		Predictions:   predictions,
	}, nil
}

// DetectSpendingPatterns identifies spending patterns
func (uc *aiAnalysisUseCase) DetectSpendingPatterns(ctx context.Context, bookID string, startTime, endTime string) ([]domain.SpendingPattern, error) {
	bills, err := uc.repository.GetBillsForAnalysis(ctx, bookID, startTime, endTime)
	if err != nil {
		return nil, err
	}

	patterns := []domain.SpendingPattern{}

	// Group bills by category and analyze patterns
	categoryBills := make(map[string][]domain.Bill)
	for _, bill := range bills {
		if bill.Type == 0 { // Only expenses
			categoryBills[bill.Category] = append(categoryBills[bill.Category], bill)
		}
	}

	for category, bills := range categoryBills {
		if len(bills) < 3 {
			continue
		}

		// Calculate average amount
		var totalAmount float64
		for _, bill := range bills {
			totalAmount += parseAmount(bill.Money)
		}
		avgAmount := totalAmount / float64(len(bills))

		// Determine frequency
		frequency := "irregular"
		pattern := "irregular"
		
		if len(bills) >= 4 {
			// Check if regular
			daysCount := make(map[int]int)
			for _, bill := range bills {
				if t, err := time.Parse("2006-01-02", bill.Time); err == nil {
					daysCount[t.Day()]++
				}
			}
			
			// If transactions happen on similar days, it's regular
			maxCount := 0
			for _, count := range daysCount {
				if count > maxCount {
					maxCount = count
				}
			}
			
			if maxCount >= len(bills)/2 {
				pattern = "regular"
				frequency = "monthly"
			}
		}

		description := fmt.Sprintf("%s类别的支出", category)
		if pattern == "regular" {
			description = fmt.Sprintf("每月定期在%s类别消费，平均金额%.2f元", category, avgAmount)
		}

		patterns = append(patterns, domain.SpendingPattern{
			Pattern:     pattern,
			Frequency:   frequency,
			AvgAmount:   avgAmount,
			Category:    category,
			Description: description,
		})
	}

	return patterns, nil
}

// Helper functions

func parseAmount(moneyStr string) float64 {
	var amount float64
	fmt.Sscanf(moneyStr, "%f", &amount)
	return amount
}

func mustParseObjectID(id string) primitive.ObjectID {
	objID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		// Return zero ObjectID on error rather than panicking
		// This allows graceful degradation
		return primitive.ObjectID{}
	}
	return objID
}

func analyzeSpendingTrend(monthlyExpenses map[string]float64) string {
	if len(monthlyExpenses) < 2 {
		return "stable"
	}

	months := make([]string, 0, len(monthlyExpenses))
	for month := range monthlyExpenses {
		months = append(months, month)
	}
	sort.Strings(months)

	// Compare first half with second half
	midPoint := len(months) / 2
	var firstHalf, secondHalf float64
	
	for i, month := range months {
		if i < midPoint {
			firstHalf += monthlyExpenses[month]
		} else {
			secondHalf += monthlyExpenses[month]
		}
	}

	if midPoint > 0 {
		firstAvg := firstHalf / float64(midPoint)
		secondAvg := secondHalf / float64(len(months)-midPoint)
		
		// Avoid division by zero
		if firstAvg > 0 {
			ratio := secondAvg / firstAvg
			if ratio > 1.15 {
				return "increasing"
			} else if ratio < 0.85 {
				return "decreasing"
			}
		} else if secondAvg > 0 {
			// If first half is 0 but second half has expenses, it's increasing
			return "increasing"
		}
	}

	return "stable"
}

func generateFinancialInsights(totalExpense, totalIncome, savingsRate float64, topCategories []domain.CategorySpending, trend string) []domain.FinancialInsight {
	insights := []domain.FinancialInsight{}

	// Savings rate insight
	if savingsRate < 0 {
		insights = append(insights, domain.FinancialInsight{
			Type:        "warning",
			Title:       "支出超过收入",
			Description: fmt.Sprintf("您的支出(%.2f)超过了收入(%.2f)，建议控制消费或增加收入来源", totalExpense, totalIncome),
			Priority:    5,
			Tags:        []string{"储蓄", "预警"},
		})
	} else if savingsRate < 20 {
		insights = append(insights, domain.FinancialInsight{
			Type:        "suggestion",
			Title:       "储蓄率偏低",
			Description: fmt.Sprintf("您的储蓄率为%.1f%%，建议提高到20%%以上", savingsRate),
			Priority:    4,
			Tags:        []string{"储蓄", "建议"},
		})
	} else if savingsRate >= 30 {
		insights = append(insights, domain.FinancialInsight{
			Type:        "insight",
			Title:       "储蓄表现优秀",
			Description: fmt.Sprintf("您的储蓄率达到%.1f%%，财务状况良好", savingsRate),
			Priority:    2,
			Tags:        []string{"储蓄", "表扬"},
		})
	}

	// Spending trend insight
	if trend == "increasing" {
		insights = append(insights, domain.FinancialInsight{
			Type:        "warning",
			Title:       "支出呈上升趋势",
			Description: "最近几个月的支出呈上升趋势，建议审查各类别开支并制定预算计划",
			Priority:    4,
			Tags:        []string{"趋势", "预警"},
		})
	} else if trend == "decreasing" {
		insights = append(insights, domain.FinancialInsight{
			Type:        "insight",
			Title:       "支出下降趋势",
			Description: "您的支出呈下降趋势，继续保持！",
			Priority:    2,
			Tags:        []string{"趋势", "积极"},
		})
	}

	// Top category insights
	if len(topCategories) > 0 {
		topCat := topCategories[0]
		if topCat.Percent > 40 {
			insights = append(insights, domain.FinancialInsight{
				Type:        "warning",
				Title:       fmt.Sprintf("%s支出占比过高", topCat.Category),
				Description: fmt.Sprintf("%s支出占总支出的%.1f%%，建议分散消费或寻找替代方案", topCat.Category, topCat.Percent),
				Priority:    3,
				Tags:        []string{"类别分析", "建议"},
			})
		}
	}

	// Sort insights by priority
	sort.Slice(insights, func(i, j int) bool {
		return insights[i].Priority > insights[j].Priority
	})

	return insights
}

func generateFinancialSummary(totalExpense, totalIncome, savingsRate float64, trend string) string {
	trendText := "保持稳定"
	if trend == "increasing" {
		trendText = "呈上升趋势"
	} else if trend == "decreasing" {
		trendText = "呈下降趋势"
	}

	return fmt.Sprintf("总支出: ¥%.2f, 总收入: ¥%.2f, 储蓄率: %.1f%%, 支出趋势: %s。"+
		"建议: 保持良好的记账习惯，定期审查支出类别，设置月度预算目标。",
		totalExpense, totalIncome, savingsRate, trendText)
}
