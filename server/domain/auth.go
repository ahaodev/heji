package domain

import (
	"context"
)

// LoginRequest 登录请求
type LoginRequest struct {
	UserName string `form:"username" binding:"required"`
	Password string `form:"password" binding:"required"`
}

// LoginResponse 登录响应
type LoginResponse struct {
	AccessToken  string `json:"accessToken"`
	RefreshToken string `json:"refreshToken"`
}

// RefreshTokenRequest 刷新令牌请求
type RefreshTokenRequest struct {
	RefreshToken string `form:"refreshToken" binding:"required"`
}

// RefreshTokenResponse 刷新令牌响应
type RefreshTokenResponse struct {
	AccessToken  string `json:"accessToken"`
	RefreshToken string `json:"refreshToken"`
}

// ProfileUpdate 个人资料更新请求
type ProfileUpdate struct {
	Name   string `json:"name"`
	Avatar string `json:"avatar"`
}

// PasswordUpdate 密码更新请求
type PasswordUpdate struct {
	CurrentPassword string `json:"current_password"`
	NewPassword     string `json:"new_password"`
}

// LogoutRequest 登出请求
type LogoutRequest struct {
	RefreshToken string `json:"refresh_token,omitempty"` // 可选的刷新令牌，用于更完整的登出处理
}

// RegisterRequest 客户端注册请求
type RegisterRequest struct {
	Name     string `json:"name" binding:"required"`
	Tel      string `json:"tel" binding:"required"`
	Password string `json:"password" binding:"required"`
}

type LoginUsecase interface {
	GetUserByUserName(c context.Context, name string) (*User, error)
	GetUserByPhone(c context.Context, phone string) (*User, error)
	GetUserByID(c context.Context, id string) (*User, error)
}

type RegisterUsecase interface {
	CreateUser(c context.Context, user *User) error
	GetUserByUserName(c context.Context, name string) (*User, error)
}
