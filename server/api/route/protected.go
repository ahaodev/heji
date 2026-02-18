package route

import (
	"shadmin/api/middleware"
	bootstrap "shadmin/bootstrap"
	"time"

	"github.com/gin-gonic/gin"
)

// ProtectedRoutes manages all authenticated routes
type ProtectedRoutes struct {
	factory *ControllerFactory
}

// NewProtectedRoutes creates a new protected routes manager
func NewProtectedRoutes(app *bootstrap.Application, timeout time.Duration) *ProtectedRoutes {
	return &ProtectedRoutes{
		factory: NewControllerFactory(app, timeout, app.DB, app.SyncService),
	}
}

// Setup configures all protected routes with authentication middleware
func (pr *ProtectedRoutes) Setup(router *gin.RouterGroup, app *bootstrap.Application, engine *gin.Engine) {
	// Apply JWT authentication middleware
	protectedRouter := router.Group("")
	protectedRouter.Use(middleware.JwtAuthMiddleware(app.Env.AccessTokenSecret))

	// Setup different route groups
	pr.setupUserRoutes(protectedRouter, app)
	pr.setupSystemRoutes(protectedRouter, app, engine)
	pr.setupHejiRoutes(protectedRouter, middleware.NewCasbinMiddleware(app.CasManager))
}

// setupUserRoutes configures basic user functionality routes
func (pr *ProtectedRoutes) setupUserRoutes(router *gin.RouterGroup, app *bootstrap.Application) {
	// Profile management
	profileGroup := router.Group("/profile")
	pr.setupProfileRoutes(profileGroup)

	// Resource menu access
	resourcesGroup := router.Group("/resources")
	pr.setupResourceRoutes(resourcesGroup)

}

// setupProfileRoutes configures profile management routes
func (pr *ProtectedRoutes) setupProfileRoutes(group *gin.RouterGroup) {
	profileController := pr.factory.CreateProfileController()

	group.GET("/", profileController.GetProfile)
	group.PUT("/", profileController.UpdateProfile)
	group.PUT("/password", profileController.UpdatePassword)
}

// setupResourceRoutes configures resource access routes
func (pr *ProtectedRoutes) setupResourceRoutes(group *gin.RouterGroup) {
	resourceController := pr.factory.CreateResourceController()

	group.GET("", resourceController.GetResources)
}

// setupSystemRoutes configures system administration routes
func (pr *ProtectedRoutes) setupSystemRoutes(router *gin.RouterGroup, app *bootstrap.Application, engine *gin.Engine) {
	casbinMiddleware := middleware.NewCasbinMiddleware(app.CasManager)
	systemGroup := router.Group("/system")

	// Setup system route groups
	pr.setupUserManagement(systemGroup, casbinMiddleware)
	pr.setupRoleManagement(systemGroup, casbinMiddleware)
	pr.setupMenuManagement(systemGroup, casbinMiddleware)
	pr.setupApiResourceManagement(systemGroup, casbinMiddleware, engine)
	pr.setupLoginLogManagement(systemGroup, casbinMiddleware)
	pr.setupDictionaryManagement(systemGroup, casbinMiddleware)
}
