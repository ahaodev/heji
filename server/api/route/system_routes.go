package route

import (
	"shadmin/api/middleware"

	"github.com/gin-gonic/gin"
)

// setupUserManagement configures user management routes with unified API permission check
func (pr *ProtectedRoutes) setupUserManagement(systemGroup *gin.RouterGroup, casbinMiddleware *middleware.CasbinMiddleware) {
	userGroup := systemGroup.Group("/user")
	userGroup.Use(casbinMiddleware.CheckAPIPermission())
	userController := pr.factory.CreateUserController()

	userGroup.GET("/", userController.GetUsers)
	userGroup.POST("/", userController.CreateUser)

	userGroup.GET("/:id", userController.GetUser)
	userGroup.PUT("/:id", userController.UpdateUser)
	userGroup.DELETE("/:id", userController.DeleteUser)

	userGroup.POST("/invite", userController.InviteUser)
	userGroup.GET("/:id/roles", userController.GetUserRoles)
}

// setupRoleManagement configures role management routes with unified API permission check
func (pr *ProtectedRoutes) setupRoleManagement(systemGroup *gin.RouterGroup, casbinMiddleware *middleware.CasbinMiddleware) {
	roleGroup := systemGroup.Group("/role")
	roleGroup.Use(casbinMiddleware.CheckAPIPermission())
	roleController := pr.factory.CreateRoleController()

	// CRUD operations
	roleGroup.GET("", roleController.GetRoles)          // GET /api/v1/system/role
	roleGroup.POST("", roleController.CreateRole)       // POST /api/v1/system/role
	roleGroup.GET("/:id", roleController.GetRole)       // GET /api/v1/system/role/:id
	roleGroup.PUT("/:id", roleController.UpdateRole)    // PUT /api/v1/system/role/:id
	roleGroup.DELETE("/:id", roleController.DeleteRole) // DELETE /api/v1/system/role/:id

	roleGroup.GET("/:id/menus", roleController.GetRoleMenus) // GET /api/v1/system/role/menus/:id
}

// setupMenuManagement configures menu management routes with unified API permission check
func (pr *ProtectedRoutes) setupMenuManagement(systemGroup *gin.RouterGroup, casbinMiddleware *middleware.CasbinMiddleware) {
	menuGroup := systemGroup.Group("/menu")
	menuGroup.Use(casbinMiddleware.CheckAPIPermission())
	menuController := pr.factory.CreateMenuController()

	menuGroup.GET("", menuController.GetMenus)
	menuGroup.POST("", menuController.CreateMenu)
	menuGroup.GET("/:id", menuController.GetMenu)
	menuGroup.PUT("/:id", menuController.UpdateMenu)
	menuGroup.DELETE("/:id", menuController.DeleteMenu)

	menuGroup.GET("/tree", menuController.GetMenuTree)
}

// setupApiResourceManagement configures API resource management routes with unified API permission check
func (pr *ProtectedRoutes) setupApiResourceManagement(systemGroup *gin.RouterGroup, casbinMiddleware *middleware.CasbinMiddleware, engine *gin.Engine) {
	apiGroup := systemGroup.Group("/api-resources")
	apiGroup.Use(casbinMiddleware.CheckAPIPermission())
	apiResourceController := pr.factory.CreateApiResourceController(engine)

	apiGroup.GET("", apiResourceController.GetApiResources)
}

// setupLoginLogManagement configures login log management routes with unified API permission check
func (pr *ProtectedRoutes) setupLoginLogManagement(systemGroup *gin.RouterGroup, casbinMiddleware *middleware.CasbinMiddleware) {
	loginLogGroup := systemGroup.Group("/login-logs")
	loginLogGroup.Use(casbinMiddleware.CheckAPIPermission())
	loginLogController := pr.factory.CreateLoginLogController()

	loginLogGroup.GET("", loginLogController.GetLoginLogs)      // GET /api/v1/system/login-logs
	loginLogGroup.DELETE("", loginLogController.ClearLoginLogs) // DELETE /api/v1/system/login-logs
}

// setupDictionaryManagement configures dictionary management routes with unified API permission check
func (pr *ProtectedRoutes) setupDictionaryManagement(systemGroup *gin.RouterGroup, casbinMiddleware *middleware.CasbinMiddleware) {
	dictGroup := systemGroup.Group("/dict")
	dictGroup.Use(casbinMiddleware.CheckAPIPermission())
	dictController := pr.factory.CreateDictController()

	// Dictionary type CRUD operations
	dictGroup.GET("/types", dictController.GetDictTypes)          // GET /api/v1/system/dict/types
	dictGroup.POST("/types", dictController.CreateDictType)       // POST /api/v1/system/dict/types
	dictGroup.GET("/types/:id", dictController.GetDictType)       // GET /api/v1/system/dict/types/:id
	dictGroup.PUT("/types/:id", dictController.UpdateDictType)    // PUT /api/v1/system/dict/types/:id
	dictGroup.DELETE("/types/:id", dictController.DeleteDictType) // DELETE /api/v1/system/dict/types/:id

	// Dictionary item CRUD operations
	dictGroup.GET("/items", dictController.GetDictItems)          // GET /api/v1/system/dict/items
	dictGroup.POST("/items", dictController.CreateDictItem)       // POST /api/v1/system/dict/items
	dictGroup.GET("/items/:id", dictController.GetDictItem)       // GET /api/v1/system/dict/items/:id
	dictGroup.PUT("/items/:id", dictController.UpdateDictItem)    // PUT /api/v1/system/dict/items/:id
	dictGroup.DELETE("/items/:id", dictController.DeleteDictItem) // DELETE /api/v1/system/dict/items/:id

	// Convenience API
	dictGroup.GET("/types/code/:code/items", dictController.GetDictItemsByTypeCode) // GET /api/v1/system/dict/types/code/:code/items
}
