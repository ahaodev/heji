package middleware

import (
	"fmt"
	"net/http"
	"shadmin/domain"
	"shadmin/internal/casbin"
	"shadmin/internal/constants"
	"strings"

	"github.com/gin-gonic/gin"
)

// CasbinMiddleware API权限鉴权中间件
// 职责：仅鉴权用户是否有访问特定API的权限
type CasbinMiddleware struct {
	CasManager casbin.Manager
}

// NewCasbinMiddleware 创建新的 Casbin 中间件实例
func NewCasbinMiddleware(CasManager casbin.Manager) *CasbinMiddleware {
	return &CasbinMiddleware{
		CasManager: CasManager,
	}
}

// CheckAPIPermission API网关权限检查中间件
func (m *CasbinMiddleware) CheckAPIPermission() gin.HandlerFunc {
	return func(c *gin.Context) {
		userID := c.GetString(constants.UserID)

		path := c.Request.URL.Path
		method := c.Request.Method

		fmt.Printf("🔍 API权限检查> 用户ID %s, %s %s\n", userID, method, path)

		// 跳过不需要权限校验的API
		if m.shouldSkipPermissionCheck(path) {
			fmt.Printf("✅ 跳过权限检查: %s\n", path)
			c.Next()
			return
		}

		// 包装用户ID后传给 Manager
		hasPermission, err := m.CasManager.CheckPermission(userID, path, method)

		fmt.Printf("🔍 检查结果: %t, error=%v\n", hasPermission, err)

		if err != nil {
			fmt.Printf("❌ 权限检查错误: %v\n", err)
			c.JSON(http.StatusInternalServerError, domain.RespError("权限检查失败"))
			c.Abort()
			return
		}

		if !hasPermission {
			c.JSON(http.StatusForbidden, domain.RespError("权限不足"))
			c.Abort()
			return
		}

		c.Next()
	}
}

// shouldSkipPermissionCheck 判断是否跳过权限检查
func (m *CasbinMiddleware) shouldSkipPermissionCheck(path string) bool {
	// 使用集中管理的常量来跳过权限检查
	skipPaths := constants.GetAPIPathsToSkipPermissionCheck()

	for _, skipPath := range skipPaths {
		if strings.HasPrefix(path, skipPath) {
			return true
		}
	}

	return false
}
