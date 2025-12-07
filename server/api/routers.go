package api

import (
	"github.com/gin-gonic/gin"
	"heji-server/api/controller"
	"heji-server/config"
	"heji-server/domain"
	"heji-server/internal/get"
	"heji-server/mongo"
	"heji-server/repository"
	"heji-server/usecase"
	"time"
)

// RegisterRoutes configures the available web server routes.
func RegisterRoutes(conf *config.Config, db mongo.Database) {
	timeout := get.Config().Mongo.TimeoutMax
	NewWebSocket(db, timeout, APIv1)
	NewUserRouter(db, timeout, APIv1)
	NewBookRouter(db, timeout, APIv1)
	NewAIAnalysisRouter(db, timeout, APIv1)
}
func NewWebSocket(db mongo.Database, timeout time.Duration, group *gin.RouterGroup) {
	ws := repository.NewMessagesRepository(db, domain.CollMessage)
	buc := repository.NewBillRepository(db, domain.CollBill)
	bookRepository := repository.NewBookRepository(db, domain.CollBook)
	sharedRepository := repository.NewSharedRepository(db, domain.CollShared)
	wsc := &controller.WSController{
		MessageUseCase: usecase.NewMessageUseCase(ws),
		BillUseCase:    usecase.NewBillUseCase(buc),
		BookUseCase:    usecase.NewBookUseCase(bookRepository, sharedRepository, time.Duration(1)),
	}
	group.GET("/ws", wsc.Upgrade)
}
func NewUserRouter(db mongo.Database, timeout time.Duration, group *gin.RouterGroup) {
	ur := repository.NewUserRepository(db, domain.CollUser)
	lc := &controller.UserController{
		UserUseCase: usecase.NewLoginUseCase(ur, timeout),
	}
	group.POST("/Register", lc.Register)
	group.POST("/Login", lc.Login)
}
func NewBookRouter(db mongo.Database, timeout time.Duration, group *gin.RouterGroup) {
	br := repository.NewBookRepository(db, domain.CollBook)
	sr := repository.NewSharedRepository(db, domain.CollShared)
	bc := &controller.BookController{
		UseCase: usecase.NewBookUseCase(br, sr, timeout),
	}
	group.POST("/CreateBook", bc.CreateBook)
	group.POST("/BookList", bc.BookList)
	group.POST("/DeleteBook/:bid", bc.DeleteBook)
	group.POST("/UpdateBook", bc.UpdateBook)
	group.POST("/SharedBook/:bid", bc.SharedBook)
	group.POST("/JoinBook/:code", bc.JoinBook)
}

func NewAIAnalysisRouter(db mongo.Database, timeout time.Duration, group *gin.RouterGroup) {
	ar := repository.NewAIAnalysisRepository(db, domain.CollAIPrediction)
	ac := &controller.AIAnalysisController{
		UseCase: usecase.NewAIAnalysisUseCase(ar),
	}
	group.POST("/ai/classify", ac.ClassifyBill)
	group.GET("/ai/predict", ac.PredictExpenses)
	group.POST("/ai/analyze", ac.AnalyzeFinancials)
	group.GET("/ai/patterns", ac.DetectPatterns)
}
