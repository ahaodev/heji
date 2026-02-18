package route

import (
	"shadmin/api/middleware"
	bootstrap "shadmin/bootstrap"
	"shadmin/domain"
	"time"

	"github.com/gin-gonic/gin"
)

// PublicRoutes manages all public (unauthenticated) routes
type PublicRoutes struct {
	factory *ControllerFactory
}

// NewPublicRoutes creates a new public routes manager
func NewPublicRoutes(app *bootstrap.Application, timeout time.Duration) *PublicRoutes {
	return &PublicRoutes{
		factory: NewControllerFactory(app, timeout, app.DB, app.SyncService),
	}
}

// Setup configures all public routes
func (pr *PublicRoutes) Setup(router *gin.RouterGroup, app *bootstrap.Application) {
	// Add development logging middleware
	if app.Env.AppEnv == "development" {
		router.Use(middleware.LogMiddleware())
	}

	// Authentication routes
	authGroup := router.Group("/auth")
	pr.setupAuthRoutes(authGroup, app)

	// Client-compatible routes
	pr.setupClientRoutes(router, app)
}

// setupAuthRoutes configures authentication-related routes
func (pr *PublicRoutes) setupAuthRoutes(group *gin.RouterGroup, app *bootstrap.Application) {
	authController := pr.factory.CreateAuthController(app.CasManager)

	group.POST("/login", authController.Login)
	group.POST("/refresh", authController.RefreshToken)
	group.POST("/logout", authController.Logout)
}

// setupClientRoutes configures routes compatible with heji Android client
func (pr *PublicRoutes) setupClientRoutes(group *gin.RouterGroup, app *bootstrap.Application) {
	authController := pr.factory.CreateAuthController(app.CasManager)

	group.POST("/Register", authController.Register)
	group.POST("/Login", authController.ClientLogin)
	group.GET("/mqtt/broker", func(c *gin.Context) {
		c.JSON(200, domain.RespSuccess(gin.H{
			"address":  app.Env.MQTTAddress,
			"tcp_port": app.Env.MQTTTCPPort,
			"ws_port":  app.Env.MQTTWSPort,
		}))
	})
}
