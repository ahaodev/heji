//go:build production
// +build production

package route

import (
	"github.com/gin-gonic/gin"
)

// setupSwagger 生产环境下不设置Swagger文档
func setupSwagger(gin *gin.Engine) {
	// 生产环境不启用Swagger
}
