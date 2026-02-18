package domain

import (
	"context"
)

// Category 分类实体
type Category struct {
	ID         string `json:"_id"`
	BookID     string `json:"book_id"`
	Name       string `json:"name"`
	Type       int    `json:"type"`
	Level      int    `json:"level"`
	Index      int    `json:"index"`
	ParentID   string `json:"parentId,omitempty"`
	CreateUser string `json:"create_user,omitempty"`
}

// CreateCategoryRequest 创建分类请求
type CreateCategoryRequest struct {
	ID       string `json:"_id,omitempty"`
	BookID   string `json:"book_id" binding:"required"`
	Name     string `json:"name" binding:"required"`
	Type     int    `json:"type" binding:"required"`
	Level    int    `json:"level,omitempty"`
	Index    int    `json:"index,omitempty"`
	ParentID string `json:"parentId,omitempty"`
}

// UpdateCategoryRequest 更新分类请求
type UpdateCategoryRequest struct {
	Name     *string `json:"name,omitempty"`
	Type     *int    `json:"type,omitempty"`
	Level    *int    `json:"level,omitempty"`
	Index    *int    `json:"index,omitempty"`
	ParentID *string `json:"parentId,omitempty"`
}

// CategoryRepository 分类数据访问接口
type CategoryRepository interface {
	Create(c context.Context, category *Category) error
	GetByID(c context.Context, id string) (*Category, error)
	ListByBookID(c context.Context, bookID string) ([]*Category, error)
	Update(c context.Context, category *Category) error
	Delete(c context.Context, id string) error
}

// CategoryUseCase 分类业务逻辑接口
type CategoryUseCase interface {
	CreateCategory(c context.Context, userID string, req *CreateCategoryRequest) (*Category, error)
	CreateCategories(c context.Context, userID string, reqs []*CreateCategoryRequest) ([]*Category, error)
	GetCategory(c context.Context, id string) (*Category, error)
	ListCategories(c context.Context, bookID string) ([]*Category, error)
	UpdateCategory(c context.Context, id string, req *UpdateCategoryRequest) (*Category, error)
	DeleteCategory(c context.Context, id string) error
}
