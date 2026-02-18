package domain

import (
	"context"
	"errors"
	"time"
)

var (
	ErrInvalidPassword = errors.New("invalid password")
)

// User 结构体定义了用户的基本信息 (单租户架构)
type User struct {
	ID        string     `json:"id"`
	Username  string     `json:"username"`
	Email     string     `json:"email"`
	Phone     string     `json:"phone,omitempty"`
	Password  string     `json:"password,omitempty"`
	Avatar    string     `json:"avatar,omitempty"`
	IsAdmin   bool       `json:"is_admin,omitempty"`
	Status    string     `json:"status"`              // active, inactive, invited, suspended
	Roles     []string   `json:"role,omitempty"`      // 用户的角色
	IsActive  bool       `json:"is_active,omitempty"` // 用户是否活跃
	CreatedAt time.Time  `json:"created_at"`
	UpdatedAt time.Time  `json:"updated_at"`
	InvitedAt *time.Time `json:"invited_at,omitempty"`
	InvitedBy string     `json:"invited_by,omitempty"`
}

// CreateUserRequest 创建用户请求 (单租户架构)
type CreateUserRequest struct {
	Username string   `json:"username" binding:"required"`
	Email    string   `json:"email" binding:"required,email"`
	Phone    string   `json:"phone,omitempty"`
	Password string   `json:"password" binding:"required"`
	Status   string   `json:"status,omitempty"`   // active, inactive, invited, suspended
	RoleIDs  []string `json:"role_ids,omitempty"` // 用户角色ID列表
}

// InviteUserRequest 邀请用户请求 (单租户架构)
type InviteUserRequest struct {
	Email   string   `json:"email" binding:"required,email"`
	RoleIDs []string `json:"role_ids,omitempty"` // 角色ID列表
	Message string   `json:"message,omitempty"`  // 可选的邀请消息
}

// UserUpdateRequest 更新用户请求
type UserUpdateRequest struct {
	Username *string  `json:"username,omitempty"`
	Email    *string  `json:"email,omitempty"`
	Phone    *string  `json:"phone,omitempty"`
	Password *string  `json:"password,omitempty"`
	Status   *string  `json:"status,omitempty"`
	Avatar   *string  `json:"avatar,omitempty"`
	RoleIDs  []string `json:"role_ids,omitempty"`
}

// UserPagedResult 用户分页查询结果
type UserPagedResult = PagedResult[*User]

// UserQueryFilter 用户查询过滤器 (单租户架构)
type UserQueryFilter struct {
	Status       string `json:"status,omitempty"`
	Role         string `json:"role,omitempty"`
	Username     string `json:"username,omitempty"`
	Email        string `json:"email,omitempty"`
	IsAdmin      *bool  `json:"is_admin,omitempty"`
	IncludeRoles bool   `json:"include_roles,omitempty"`
	QueryParams
}

type UserRepository interface {
	Create(c context.Context, user *User) error
	GetByID(c context.Context, id string) (*User, error)
	GetByUsername(c context.Context, username string) (*User, error)
	GetByEmail(c context.Context, email string) (*User, error)
	GetByPhone(c context.Context, phone string) (*User, error)
	Query(c context.Context, filter UserQueryFilter) (*PagedResult[*User], error)
	Update(c context.Context, user *User) error
	Delete(c context.Context, id string) error
}

type UserUseCase interface {
	CreateUser(c context.Context, request *CreateUserRequest) (*User, error)
	GetUserByID(c context.Context, id string) (*User, error)
	ListUsers(c context.Context, filter UserQueryFilter) (*PagedResult[*User], error)
	UpdateUserProfile(c context.Context, userID string, updates ProfileUpdate) error
	UpdateUserPassword(c context.Context, userID string, passwordUpdate PasswordUpdate) error
	UpdateUserPartial(c context.Context, userID string, updates UserUpdateRequest) error
	DeleteUser(c context.Context, id string) error
	InviteUser(c context.Context, request *InviteUserRequest, invitedBy string) (*User, error)
	GetUserRoles(c context.Context, userID string) ([]string, error)
}
