package usecase

import (
	"context"
	"heji-server/domain"
	"testing"
)

type mockAIRepository struct{}

func (m *mockAIRepository) SavePrediction(ctx context.Context, prediction *domain.ExpensePrediction) error {
	return nil
}

func (m *mockAIRepository) GetLatestPrediction(ctx context.Context, bookID string, period string) (*domain.ExpensePrediction, error) {
	return nil, nil
}

func (m *mockAIRepository) GetBillsForAnalysis(ctx context.Context, bookID string, startTime, endTime string) ([]domain.Bill, error) {
	return []domain.Bill{}, nil
}

func (m *mockAIRepository) GetCategoryStatistics(ctx context.Context, bookID string, startTime, endTime string) (map[string]domain.CategorySpending, error) {
	return map[string]domain.CategorySpending{}, nil
}

func TestClassifyBill_RestaurantExpense(t *testing.T) {
	repo := &mockAIRepository{}
	uc := NewAIAnalysisUseCase(repo)

	req := &domain.BillClassificationRequest{
		Description: "肯德基午餐",
		Amount:      45.5,
		Merchant:    "肯德基",
	}

	result, err := uc.ClassifyBill(context.Background(), req)
	if err != nil {
		t.Fatalf("ClassifyBill failed: %v", err)
	}

	if len(result.Suggestions) == 0 {
		t.Error("Expected at least one suggestion")
	}

	if result.Type != 0 {
		t.Errorf("Expected type 0 (expense), got %d", result.Type)
	}

	// Check if "餐饮" is suggested
	found := false
	for _, suggestion := range result.Suggestions {
		if suggestion.Category == "餐饮" {
			found = true
			if suggestion.Confidence < 0.7 {
				t.Errorf("Expected confidence >= 0.7 for restaurant, got %f", suggestion.Confidence)
			}
			break
		}
	}

	if !found {
		t.Error("Expected '餐饮' category to be suggested for restaurant expense")
	}
}

func TestClassifyBill_TransportExpense(t *testing.T) {
	repo := &mockAIRepository{}
	uc := NewAIAnalysisUseCase(repo)

	req := &domain.BillClassificationRequest{
		Description: "滴滴打车",
		Amount:      28.5,
		Merchant:    "滴滴出行",
	}

	result, err := uc.ClassifyBill(context.Background(), req)
	if err != nil {
		t.Fatalf("ClassifyBill failed: %v", err)
	}

	if len(result.Suggestions) == 0 {
		t.Error("Expected at least one suggestion")
	}

	// Check if "交通" is suggested
	found := false
	for _, suggestion := range result.Suggestions {
		if suggestion.Category == "交通" {
			found = true
			break
		}
	}

	if !found {
		t.Error("Expected '交通' category to be suggested for transport expense")
	}
}

func TestClassifyBill_SalaryIncome(t *testing.T) {
	repo := &mockAIRepository{}
	uc := NewAIAnalysisUseCase(repo)

	req := &domain.BillClassificationRequest{
		Description: "公司工资发放",
		Amount:      8000.0,
		Merchant:    "公司财务",
	}

	result, err := uc.ClassifyBill(context.Background(), req)
	if err != nil {
		t.Fatalf("ClassifyBill failed: %v", err)
	}

	if result.Type != 1 {
		t.Errorf("Expected type 1 (income), got %d", result.Type)
	}

	// Check if "工资" is suggested
	found := false
	for _, suggestion := range result.Suggestions {
		if suggestion.Category == "工资" {
			found = true
			break
		}
	}

	if !found {
		t.Error("Expected '工资' category to be suggested for salary income")
	}
}

func TestClassifyBill_EnglishKeywords(t *testing.T) {
	repo := &mockAIRepository{}
	uc := NewAIAnalysisUseCase(repo)

	req := &domain.BillClassificationRequest{
		Description: "Starbucks coffee",
		Amount:      32.0,
		Merchant:    "Starbucks",
	}

	result, err := uc.ClassifyBill(context.Background(), req)
	if err != nil {
		t.Fatalf("ClassifyBill failed: %v", err)
	}

	if len(result.Suggestions) == 0 {
		t.Error("Expected at least one suggestion")
	}

	// Should suggest "餐饮" for coffee shop
	found := false
	for _, suggestion := range result.Suggestions {
		if suggestion.Category == "餐饮" {
			found = true
			break
		}
	}

	if !found {
		t.Error("Expected '餐饮' category to be suggested for coffee shop")
	}
}

func TestClassifyBill_LargeAmount(t *testing.T) {
	repo := &mockAIRepository{}
	uc := NewAIAnalysisUseCase(repo)

	req := &domain.BillClassificationRequest{
		Description: "房租",
		Amount:      5000.0,
		Merchant:    "",
	}

	result, err := uc.ClassifyBill(context.Background(), req)
	if err != nil {
		t.Fatalf("ClassifyBill failed: %v", err)
	}

	// Should match housing keywords like "房租"
	found := false
	for _, suggestion := range result.Suggestions {
		if suggestion.Category == "居住" {
			found = true
			break
		}
	}

	if !found {
		t.Error("Expected '居住' category to be suggested for housing expense with keyword '房租'")
	}
}

func TestClassifyBill_NoMatch(t *testing.T) {
	repo := &mockAIRepository{}
	uc := NewAIAnalysisUseCase(repo)

	req := &domain.BillClassificationRequest{
		Description: "unknown transaction",
		Amount:      100.0,
		Merchant:    "",
	}

	result, err := uc.ClassifyBill(context.Background(), req)
	if err != nil {
		t.Fatalf("ClassifyBill failed: %v", err)
	}

	if len(result.Suggestions) == 0 {
		t.Error("Expected at least one default suggestion")
	}

	// Should suggest "其他" when no match
	if result.Suggestions[0].Category != "其他" {
		t.Errorf("Expected default '其他' category, got %s", result.Suggestions[0].Category)
	}
}

func TestAnalyzeSpendingTrend_Increasing(t *testing.T) {
	monthlyExpenses := map[string]float64{
		"2024-01": 1000.0,
		"2024-02": 1100.0,
		"2024-03": 1300.0,
		"2024-04": 1500.0,
	}

	trend := analyzeSpendingTrend(monthlyExpenses)
	if trend != "increasing" {
		t.Errorf("Expected 'increasing' trend, got %s", trend)
	}
}

func TestAnalyzeSpendingTrend_Decreasing(t *testing.T) {
	monthlyExpenses := map[string]float64{
		"2024-01": 1500.0,
		"2024-02": 1300.0,
		"2024-03": 1100.0,
		"2024-04": 900.0,
	}

	trend := analyzeSpendingTrend(monthlyExpenses)
	if trend != "decreasing" {
		t.Errorf("Expected 'decreasing' trend, got %s", trend)
	}
}

func TestAnalyzeSpendingTrend_Stable(t *testing.T) {
	monthlyExpenses := map[string]float64{
		"2024-01": 1000.0,
		"2024-02": 1050.0,
		"2024-03": 1000.0,
		"2024-04": 1020.0,
	}

	trend := analyzeSpendingTrend(monthlyExpenses)
	if trend != "stable" {
		t.Errorf("Expected 'stable' trend, got %s", trend)
	}
}

func TestParseAmount(t *testing.T) {
	tests := []struct {
		input    string
		expected float64
	}{
		{"123.45", 123.45},
		{"1000", 1000.0},
		{"0.99", 0.99},
		{"invalid", 0.0},
	}

	for _, test := range tests {
		result := parseAmount(test.input)
		if result != test.expected {
			t.Errorf("parseAmount(%s) = %f, expected %f", test.input, result, test.expected)
		}
	}
}
