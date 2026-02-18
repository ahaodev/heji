package route

import (
	"shadmin/api/middleware"

	"github.com/gin-gonic/gin"
)

// setupHejiRoutes configures book, bill, and category routes
func (pr *ProtectedRoutes) setupHejiRoutes(router *gin.RouterGroup, casbinMiddleware *middleware.CasbinMiddleware) {
	pr.setupBookRoutes(router, casbinMiddleware)
	pr.setupBillRoutes(router, casbinMiddleware)
	pr.setupCategoryRoutes(router, casbinMiddleware)
	pr.setupImageRoutes(router, casbinMiddleware)
	pr.setupSyncRoutes(router, casbinMiddleware)
}

func (pr *ProtectedRoutes) setupBookRoutes(router *gin.RouterGroup, casbinMiddleware *middleware.CasbinMiddleware) {
	bookController := pr.factory.CreateBookController()
	bookGroup := router.Group("/book")
	bookGroup.Use(casbinMiddleware.CheckAPIPermission())

	bookGroup.POST("", bookController.CreateBook)
	bookGroup.GET("", bookController.ListBooks)
	bookGroup.GET("/:id", bookController.GetBook)
	bookGroup.PUT("/:id", bookController.UpdateBook)
	bookGroup.DELETE("/:id", bookController.DeleteBook)
	bookGroup.POST("/:id/join", bookController.JoinBook)
	bookGroup.POST("/:id/share", bookController.ShareBook)
}

func (pr *ProtectedRoutes) setupBillRoutes(router *gin.RouterGroup, casbinMiddleware *middleware.CasbinMiddleware) {
	billController := pr.factory.CreateBillController()
	billGroup := router.Group("/bill")
	billGroup.Use(casbinMiddleware.CheckAPIPermission())

	billGroup.POST("", billController.CreateBill)
	billGroup.GET("", billController.ListBills)
	billGroup.GET("/export", billController.ExportBills)
	billGroup.GET("/:id", billController.GetBill)
	billGroup.PUT("/:id", billController.UpdateBill)
	billGroup.DELETE("/:id", billController.DeleteBill)
}

func (pr *ProtectedRoutes) setupCategoryRoutes(router *gin.RouterGroup, casbinMiddleware *middleware.CasbinMiddleware) {
	categoryController := pr.factory.CreateCategoryController()
	categoryGroup := router.Group("/category")
	categoryGroup.Use(casbinMiddleware.CheckAPIPermission())

	categoryGroup.POST("", categoryController.CreateCategory)
	categoryGroup.POST("/batch", categoryController.CreateCategories)
	categoryGroup.GET("", categoryController.ListCategories)
	categoryGroup.GET("/:id", categoryController.GetCategory)
	categoryGroup.PUT("/:id", categoryController.UpdateCategory)
	categoryGroup.DELETE("/:id", categoryController.DeleteCategory)
}

func (pr *ProtectedRoutes) setupImageRoutes(router *gin.RouterGroup, casbinMiddleware *middleware.CasbinMiddleware) {
	imageController := pr.factory.CreateImageController()
	imageGroup := router.Group("/image")
	imageGroup.Use(casbinMiddleware.CheckAPIPermission())

	imageGroup.POST("/upload", imageController.UploadImage)
	imageGroup.GET("/list", imageController.GetBillImages)
	imageGroup.GET("/:id", imageController.GetImage)
	imageGroup.DELETE("", imageController.DeleteImage)
}

func (pr *ProtectedRoutes) setupSyncRoutes(router *gin.RouterGroup, casbinMiddleware *middleware.CasbinMiddleware) {
	syncController := pr.factory.CreateSyncController()
	syncGroup := router.Group("/sync")
	syncGroup.Use(casbinMiddleware.CheckAPIPermission())

	syncGroup.GET("/changes", syncController.GetChanges)
}
