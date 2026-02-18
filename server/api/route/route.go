package route

import (
	bootstrap "shadmin/bootstrap"
	"shadmin/web"
	"time"

	"github.com/gin-gonic/gin"
)

const ApiUri = "/api/v1"

// Setup initializes and configures all routes and server settings
func Setup(app *bootstrap.Application, timeout time.Duration, engine *gin.Engine) error {
	// Apply server configuration
	config := DefaultServerConfig()
	if err := config.Apply(engine); err != nil {
		return err
	}

	// Register static assets and Swagger documentation
	web.Register(engine)
	setupSwagger(engine)

	// Setup API routes
	setupApiRoutes(app, timeout, engine)

	return nil
}

// setupApiRoutes configures all API routes
func setupApiRoutes(app *bootstrap.Application, timeout time.Duration, engine *gin.Engine) {
	apiV1 := engine.Group(ApiUri)

	// Setup public routes (no authentication required)
	publicRoutes := NewPublicRoutes(app, timeout)
	publicRoutes.Setup(apiV1, app)

	// Setup protected routes (authentication required)
	protectedRoutes := NewProtectedRoutes(app, timeout)
	protectedRoutes.Setup(apiV1, app, engine)
}
