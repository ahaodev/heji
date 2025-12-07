package controller

import (
	"heji-server/domain"
	"net/http"

	"github.com/gin-gonic/gin"
)

type AIAnalysisController struct {
	UseCase domain.AIAnalysisUseCase
}

// ClassifyBill handles bill classification requests
// @Summary Classify a bill using AI
// @Description Automatically classify a bill based on description, amount, and merchant
// @Tags AI
// @Accept json
// @Produce json
// @Param request body domain.BillClassificationRequest true "Bill details for classification"
// @Success 200 {object} domain.BillClassificationResponse
// @Failure 400 {object} domain.ErrorResponse
// @Router /api/v1/ai/classify [post]
func (c *AIAnalysisController) ClassifyBill(ctx *gin.Context) {
	var req domain.BillClassificationRequest
	
	if err := ctx.ShouldBindJSON(&req); err != nil {
		ctx.JSON(http.StatusBadRequest, domain.ErrorResponse{
			Message: "Invalid request: " + err.Error(),
		})
		return
	}

	result, err := c.UseCase.ClassifyBill(ctx.Request.Context(), &req)
	if err != nil {
		ctx.JSON(http.StatusInternalServerError, domain.ErrorResponse{
			Message: "Classification failed: " + err.Error(),
		})
		return
	}

	ctx.JSON(http.StatusOK, result)
}

// PredictExpenses handles expense prediction requests
// @Summary Predict future expenses
// @Description Predict expenses for a given period based on historical data
// @Tags AI
// @Accept json
// @Produce json
// @Param book_id query string true "Book ID"
// @Param period query string true "Period (e.g., 2024-01)"
// @Success 200 {object} domain.ExpensePrediction
// @Failure 400 {object} domain.ErrorResponse
// @Router /api/v1/ai/predict [get]
func (c *AIAnalysisController) PredictExpenses(ctx *gin.Context) {
	bookID := ctx.Query("book_id")
	period := ctx.Query("period")

	if bookID == "" || period == "" {
		ctx.JSON(http.StatusBadRequest, domain.ErrorResponse{
			Message: "book_id and period are required",
		})
		return
	}

	prediction, err := c.UseCase.PredictExpenses(ctx.Request.Context(), bookID, period)
	if err != nil {
		ctx.JSON(http.StatusInternalServerError, domain.ErrorResponse{
			Message: "Prediction failed: " + err.Error(),
		})
		return
	}

	ctx.JSON(http.StatusOK, prediction)
}

// AnalyzeFinancials handles financial analysis requests
// @Summary Analyze financial data
// @Description Provide comprehensive financial analysis including insights and recommendations
// @Tags AI
// @Accept json
// @Produce json
// @Param request body domain.FinancialAnalysisRequest true "Analysis parameters"
// @Success 200 {object} domain.FinancialAnalysisResponse
// @Failure 400 {object} domain.ErrorResponse
// @Router /api/v1/ai/analyze [post]
func (c *AIAnalysisController) AnalyzeFinancials(ctx *gin.Context) {
	var req domain.FinancialAnalysisRequest
	
	if err := ctx.ShouldBindJSON(&req); err != nil {
		ctx.JSON(http.StatusBadRequest, domain.ErrorResponse{
			Message: "Invalid request: " + err.Error(),
		})
		return
	}

	result, err := c.UseCase.AnalyzeFinancials(ctx.Request.Context(), &req)
	if err != nil {
		ctx.JSON(http.StatusInternalServerError, domain.ErrorResponse{
			Message: "Analysis failed: " + err.Error(),
		})
		return
	}

	ctx.JSON(http.StatusOK, result)
}

// DetectPatterns handles spending pattern detection
// @Summary Detect spending patterns
// @Description Identify recurring spending patterns and habits
// @Tags AI
// @Accept json
// @Produce json
// @Param book_id query string true "Book ID"
// @Param start_time query string false "Start time (YYYY-MM-DD)"
// @Param end_time query string false "End time (YYYY-MM-DD)"
// @Success 200 {array} domain.SpendingPattern
// @Failure 400 {object} domain.ErrorResponse
// @Router /api/v1/ai/patterns [get]
func (c *AIAnalysisController) DetectPatterns(ctx *gin.Context) {
	bookID := ctx.Query("book_id")
	startTime := ctx.DefaultQuery("start_time", "")
	endTime := ctx.DefaultQuery("end_time", "")

	if bookID == "" {
		ctx.JSON(http.StatusBadRequest, domain.ErrorResponse{
			Message: "book_id is required",
		})
		return
	}

	patterns, err := c.UseCase.DetectSpendingPatterns(ctx.Request.Context(), bookID, startTime, endTime)
	if err != nil {
		ctx.JSON(http.StatusInternalServerError, domain.ErrorResponse{
			Message: "Pattern detection failed: " + err.Error(),
		})
		return
	}

	ctx.JSON(http.StatusOK, patterns)
}
