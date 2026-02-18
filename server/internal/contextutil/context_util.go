package contextutil

import (
	"shadmin/internal/constants"

	"github.com/gin-gonic/gin"
)

// GetUserID 从gin context中获取用户ID
func GetUserID(c *gin.Context) string {
	if userID, exists := c.Get(constants.UserID); exists {
		if userIDStr, ok := userID.(string); ok {
			return userIDStr
		}
	}
	return ""
}

// GetUserName 从gin context中获取用户名
func GetUserName(c *gin.Context) string {
	if userName, exists := c.Get(constants.UserName); exists {
		if userNameStr, ok := userName.(string); ok {
			return userNameStr
		}
	}
	return ""
}

// GetUserEmail 从gin context中获取用户邮箱
func GetUserEmail(c *gin.Context) string {
	if userEmail, exists := c.Get(constants.UserEmail); exists {
		if userEmailStr, ok := userEmail.(string); ok {
			return userEmailStr
		}
	}
	return ""
}

// GetIsAdmin 从gin context中获取是否为管理员
func GetIsAdmin(c *gin.Context) bool {
	if isAdmin, exists := c.Get(constants.IsAdmin); exists {
		if isAdminBool, ok := isAdmin.(bool); ok {
			return isAdminBool
		}
	}
	return false
}

// GetCurrentUser 获取当前用户的所有信息
type CurrentUser struct {
	ID      string `json:"id"`
	Name    string `json:"name"`
	Email   string `json:"email"`
	IsAdmin bool   `json:"is_admin"`
}

func GetCurrentUser(c *gin.Context) *CurrentUser {
	return &CurrentUser{
		ID:      GetUserID(c),
		Name:    GetUserName(c),
		Email:   GetUserEmail(c),
		IsAdmin: GetIsAdmin(c),
	}
}
