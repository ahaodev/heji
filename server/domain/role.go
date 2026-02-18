package domain

import (
	"context"
	"time"
)

// Role represents a custom role in the system
type Role struct {
	ID        string    `json:"id"`
	Name      string    `json:"name"`
	Sequence  int       `json:"sequence"`
	Status    string    `json:"status"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
	MenusIds  []string  `json:"menus"`
}

// CreateRoleRequest represents a request to create a new role
type CreateRoleRequest struct {
	Name     string   `json:"name" binding:"required,max=100"`
	Sequence int      `json:"sequence"`
	Status   string   `json:"status,omitempty"`
	MenuIDs  []string `json:"menu_ids,omitempty"` // Menu permissions managed through Casbin
}

// UpdateRoleRequest represents a request to update an existing role
type UpdateRoleRequest struct {
	Name     *string  `json:"name,omitempty"`
	Sequence *int     `json:"sequence,omitempty"`
	Status   *string  `json:"status,omitempty"`
	MenuIDs  []string `json:"menu_ids,omitempty"` // Menu permissions managed through Casbin
}

// RolePagedResult represents paginated role query results
type RolePagedResult = PagedResult[*Role]

// RoleFilters represents filters for role queries
type RoleFilters struct {
	Status string `json:"status,omitempty"`
	Name   string `json:"name,omitempty"`
}

// RoleUseCase defines the interface for role business logic
type RoleUseCase interface {
	Create(c context.Context, request *CreateRoleRequest) error
	Fetch(c context.Context) ([]*Role, error)
	FetchPaged(c context.Context, params QueryParams) (*RolePagedResult, error)
	GetByID(c context.Context, id string) (*Role, error)
	Update(c context.Context, id string, request *UpdateRoleRequest) (*Role, error)
	Delete(c context.Context, id string) error
}

// RoleRepository defines the interface for role data operations
type RoleRepository interface {
	Create(c context.Context, role *Role) error
	Fetch(c context.Context) ([]*Role, error)
	FetchPaged(c context.Context, params QueryParams) (*RolePagedResult, error)
	GetByID(c context.Context, id string) (*Role, error)
	GetByName(c context.Context, name string) (*Role, error)
	Update(c context.Context, role *Role) error
	Delete(c context.Context, id string) error
	Assign(c context.Context, uid, rid string) error
	Revoke(c context.Context, uid, rid string) error
}
