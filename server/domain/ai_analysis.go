package domain

import (
	"context"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

const (
	CollAIClassification = "ai_classifications" // Store AI classification history
	CollAIPrediction     = "ai_predictions"     // Store prediction results
)

// CategorySuggestion represents an AI-suggested category for a bill
type CategorySuggestion struct {
	Category   string  `json:"category"`
	Confidence float64 `json:"confidence"`
	Reason     string  `json:"reason"`
}

// BillClassificationRequest represents a request for bill classification
type BillClassificationRequest struct {
	Description string  `json:"description"`
	Amount      float64 `json:"amount"`
	Merchant    string  `json:"merchant,omitempty"`
	Time        string  `json:"time,omitempty"`
}

// BillClassificationResponse contains AI classification results
type BillClassificationResponse struct {
	Suggestions []CategorySuggestion `json:"suggestions"`
	Type        int                  `json:"type"` // 0: expense, 1: income
}

// ExpensePrediction represents predicted expenses for a period
type ExpensePrediction struct {
	ID            primitive.ObjectID     `bson:"_id,omitempty" json:"_id"`
	BookID        primitive.ObjectID     `bson:"book_id" json:"book_id"`
	UserID        string                 `bson:"user_id" json:"user_id"`
	Period        string                 `bson:"period" json:"period"` // "2024-01", "2024-Q1", etc.
	TotalExpense  float64                `bson:"total_expense" json:"total_expense"`
	TotalIncome   float64                `bson:"total_income" json:"total_income"`
	CategoryBreak map[string]float64     `bson:"category_break" json:"category_break"`
	Confidence    float64                `bson:"confidence" json:"confidence"`
	CreatedAt     int64                  `bson:"created_at" json:"created_at"`
}

// FinancialInsight represents financial analysis insights
type FinancialInsight struct {
	Type        string   `json:"type"`        // "warning", "suggestion", "insight"
	Title       string   `json:"title"`
	Description string   `json:"description"`
	Priority    int      `json:"priority"`    // 1-5, higher is more important
	Tags        []string `json:"tags"`
}

// FinancialAnalysisRequest represents a request for financial analysis
type FinancialAnalysisRequest struct {
	BookID    string `json:"book_id"`
	StartTime string `json:"start_time"`
	EndTime   string `json:"end_time"`
}

// FinancialAnalysisResponse contains comprehensive financial analysis
type FinancialAnalysisResponse struct {
	Summary           string              `json:"summary"`
	Insights          []FinancialInsight  `json:"insights"`
	SpendingTrend     string              `json:"spending_trend"` // "increasing", "decreasing", "stable"
	SavingsRate       float64             `json:"savings_rate"`
	TopCategories     []CategorySpending  `json:"top_categories"`
	Predictions       *ExpensePrediction  `json:"predictions,omitempty"`
}

// CategorySpending represents spending in a specific category
type CategorySpending struct {
	Category string  `json:"category"`
	Amount   float64 `json:"amount"`
	Count    int     `json:"count"`
	Percent  float64 `json:"percent"`
}

// SpendingPattern represents detected spending patterns
type SpendingPattern struct {
	Pattern     string  `json:"pattern"`     // "regular", "irregular", "seasonal"
	Frequency   string  `json:"frequency"`   // "daily", "weekly", "monthly"
	AvgAmount   float64 `json:"avg_amount"`
	Category    string  `json:"category"`
	Description string  `json:"description"`
}

// AIAnalysisUseCase defines the AI analysis business logic
type AIAnalysisUseCase interface {
	ClassifyBill(ctx context.Context, req *BillClassificationRequest) (*BillClassificationResponse, error)
	PredictExpenses(ctx context.Context, bookID string, period string) (*ExpensePrediction, error)
	AnalyzeFinancials(ctx context.Context, req *FinancialAnalysisRequest) (*FinancialAnalysisResponse, error)
	DetectSpendingPatterns(ctx context.Context, bookID string, startTime, endTime string) ([]SpendingPattern, error)
}

// AIAnalysisRepository defines the data layer for AI analysis
type AIAnalysisRepository interface {
	SavePrediction(ctx context.Context, prediction *ExpensePrediction) error
	GetLatestPrediction(ctx context.Context, bookID string, period string) (*ExpensePrediction, error)
	GetBillsForAnalysis(ctx context.Context, bookID string, startTime, endTime string) ([]Bill, error)
	GetCategoryStatistics(ctx context.Context, bookID string, startTime, endTime string) (map[string]CategorySpending, error)
}
