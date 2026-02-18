package middleware

import (
	"net/http"
	"shadmin/domain"
	"shadmin/internal/constants"
	"shadmin/internal/tokenutil"
	"strings"

	"github.com/gin-gonic/gin"
)

func JwtAuthMiddleware(secret string) gin.HandlerFunc {
	return func(c *gin.Context) {
		authToken := ""

		// 1. 优先从 Authorization header 获取
		authHeader := c.Request.Header.Get("Authorization")
		t := strings.Split(authHeader, " ")
		if len(t) == 2 {
			authToken = t[1]
		}

		// 2. 降级从 query parameter 获取（用于 <img> 等无法设置 header 的场景）
		if authToken == "" {
			authToken = c.Query("token")
		}

		if authToken != "" {
			authorized, err := tokenutil.IsAuthorized(authToken, secret)
			if authorized {
				// 提取所有token信息
				claims, err := tokenutil.ExtractAllClaimsFromToken(authToken, secret)
				if err != nil {
					c.JSON(http.StatusUnauthorized, domain.RespError(err.Error()))
					c.Abort()
					return
				}

				// 设置用户信息到context
				c.Set(constants.UserID, claims.ID)
				c.Set(constants.UserName, claims.Name)
				c.Set(constants.UserEmail, claims.Email)
				c.Set(constants.IsAdmin, claims.IsAdmin)
				c.Set(constants.UserRoles, claims.Roles)

				c.Next()
				return
			}
			c.JSON(http.StatusUnauthorized, domain.RespError(err.Error()))
			c.Abort()
			return
		}
		c.JSON(http.StatusUnauthorized, domain.RespError("Not authorized"))
		c.Abort()
	}
}
