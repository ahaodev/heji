package domain

import (
	"context"
	"errors"
	"time"
)

// Menu represents a menu item in the system
type Menu struct {
	ID          string    `json:"id"`
	Name        string    `json:"name"`
	Description *string   `json:"description,omitempty"`
	Sequence    int       `json:"sequence"`
	Type        string    `json:"type"` // menu/button
	Path        *string   `json:"path,omitempty"`
	Icon        string    `json:"icon"`
	Component   *string   `json:"component,omitempty"`
	RouteName   *string   `json:"route_name,omitempty"`
	Query       *string   `json:"query,omitempty"`
	IsFrame     bool      `json:"is_frame"`
	Visible     string    `json:"visible"` // show/hide
	Permissions *string   `json:"permissions,omitempty"`
	Status      string    `json:"status"` // active/inactive
	ParentID    *string   `json:"parent_id,omitempty"`
	CreatedAt   time.Time `json:"created_at"`
	UpdatedAt   time.Time `json:"updated_at"`

	// Related data
	Children     []Menu   `json:"children,omitempty"`
	ApiResources []string `json:"apiResources,omitempty"`
}

// CreateMenuRequest represents the request to create a new menu
type CreateMenuRequest struct {
	Name         string   `json:"name" binding:"required"`
	Sequence     int      `json:"sequence"`
	Type         string   `json:"type" binding:"required"` // menu/button
	Path         *string  `json:"path"`
	Icon         string   `json:"icon"`
	Component    *string  `json:"component"`
	RouteName    *string  `json:"route_name"`
	Query        *string  `json:"query"`
	IsFrame      bool     `json:"is_frame"`
	Visible      string   `json:"visible"` // show/hide
	Permissions  *string  `json:"permissions"`
	ApiResources []string `json:"apiResources"` // Extended properties
	Status       string   `json:"status"`       // active/inactive
	ParentID     *string  `json:"parent_id"`
}

// UpdateMenuRequest represents the request to update a menu
type UpdateMenuRequest struct {
	Code         string   `json:"code"`
	Name         string   `json:"name"`
	Sequence     int      `json:"sequence"`
	Type         string   `json:"type"` // menu/button
	Path         *string  `json:"path"`
	Icon         string   `json:"icon"`
	Component    *string  `json:"component"`
	RouteName    *string  `json:"route_name"`
	Query        *string  `json:"query"`
	IsFrame      bool     `json:"is_frame"`
	Visible      string   `json:"visible"` // show/hide
	Permissions  *string  `json:"permissions"`
	ParentID     *string  `json:"parent_id"`
	Remark       *string  `json:"remark"`
	Properties   *string  `json:"properties"` // Extended properties
	Status       string   `json:"status"`     // active/inactive
	ApiResources []string `json:"apiResources"`
}

// MenuTreeNode represents a menu in tree structure
type MenuTreeNode struct {
	ID           string         `json:"id"`
	Name         string         `json:"name"`
	Sequence     int            `json:"sequence"`
	Type         string         `json:"type"`
	Path         *string        `json:"path,omitempty"`
	Icon         string         `json:"icon"`
	Component    *string        `json:"component,omitempty"`
	RouteName    *string        `json:"route_name,omitempty"`
	Query        *string        `json:"query,omitempty"`
	IsFrame      bool           `json:"is_frame"`
	Visible      string         `json:"visible"`
	Permissions  *string        `json:"permissions,omitempty"`
	Properties   *string        `json:"properties,omitempty"`
	Status       string         `json:"status"`
	ParentID     *string        `json:"parent_id,omitempty"`
	Children     []MenuTreeNode `json:"children,omitempty"`
	ApiResources []string       `json:"apiResources,omitempty"`
	Roles        []string       `json:"roles,omitempty"`
}

// AssignMenuRoleRequest represents the request to assign roles to a menu
type AssignMenuRoleRequest struct {
	MenuID string   `json:"menu_id" binding:"required"`
	Roles  []string `json:"roles" binding:"required"`
}

// RemoveMenuRoleRequest represents the request to remove roles from a menu
type RemoveMenuRoleRequest struct {
	MenuID string   `json:"menu_id" binding:"required"`
	Roles  []string `json:"roles" binding:"required"`
}

// MenuQueryParams represents query parameters for menu filtering
type MenuQueryParams struct {
	QueryParams
	Type     string `form:"type"`   // menu/button
	Status   string `form:"status"` // active/inactive
	ParentID string `form:"parent_id"`
	Search   string `form:"search"` // Search in name or code
}

// ValidateMenuQueryParams validates and sets default values for menu query parameters
func ValidateMenuQueryParams(params *MenuQueryParams) error {
	// 使用通用的分页参数验证
	if err := ValidateQueryParams(&params.QueryParams); err != nil {
		return err
	}

	// 设置业务默认值
	if params.Type == "" {
		params.Type = "menu"
	}
	if params.Status == "" {
		params.Status = "active"
	}

	return nil
}

// Menu status constants
const (
	MenuStatusActive   = "active"
	MenuStatusInactive = "inactive"
)

// Menu type constants
const (
	MenuTypeMenu   = "menu"
	MenuTypeButton = "button"
)

// HTTP method constants for menu resources
const (
	HTTPMethodGet    = "GET"
	HTTPMethodPost   = "POST"
	HTTPMethodPut    = "PUT"
	HTTPMethodDelete = "DELETE"
	HTTPMethodPatch  = "PATCH"
)

// Menu error constants
var (
	ErrInvalidMenuType   = errors.New("invalid menu type")
	ErrInvalidMenuStatus = errors.New("invalid menu status")
	ErrInvalidHTTPMethod = errors.New("invalid HTTP method")
	ErrMenuNotFound      = errors.New("menu not found")
	ErrMenuHasChildren   = errors.New("cannot delete menu with children")
)

// MenuRepository defines the interface for menu data operations
type MenuRepository interface {
	// Query operations
	GetMenuTree(ctx context.Context) ([]MenuTreeNode, error)
	GetMenus(ctx context.Context, params MenuQueryParams) (*PagedResult[Menu], error)
	GetMenuByID(ctx context.Context, id string) (*Menu, error)
	GetChildrenMenus(ctx context.Context, parentID string) ([]*Menu, error)

	// CRUD operations
	CreateMenu(ctx context.Context, menu *CreateMenuRequest) (*Menu, error)
	UpdateMenu(ctx context.Context, id string, menu *UpdateMenuRequest) (*Menu, error)
	DeleteMenu(ctx context.Context, id string) error
}

// MenuUseCase defines the interface for menu business logic
type MenuUseCase interface {
	// Query operations
	GetMenuTree(ctx context.Context) ([]MenuTreeNode, error)
	GetMenus(ctx context.Context, params MenuQueryParams) (*PagedResult[Menu], error)
	GetMenuByID(ctx context.Context, id string) (*Menu, error)

	// CRUD operations
	CreateMenu(ctx context.Context, menu *CreateMenuRequest) (*Menu, error)
	UpdateMenu(ctx context.Context, id string, menu *UpdateMenuRequest) (*Menu, error)
	DeleteMenu(ctx context.Context, id string) error
}
