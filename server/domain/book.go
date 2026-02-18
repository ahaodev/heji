package domain

import (
	"context"
	"time"
)

// Book 账本实体
type Book struct {
	ID        string     `json:"_id"`
	Name      string     `json:"name"`
	Type      string     `json:"type,omitempty"`
	Banner    string     `json:"banner,omitempty"`
	CrtUserID string     `json:"crt_user_id"`
	IsInitial bool       `json:"is_initial"`
	Members   []string   `json:"members,omitempty"`
	DeletedAt *time.Time `json:"deleted_at,omitempty"`
	CreatedAt time.Time  `json:"crt_time"`
	UpdatedAt time.Time  `json:"upd_time"`
}

// CreateBookRequest 创建账本请求
type CreateBookRequest struct {
	ID        string `json:"_id,omitempty"`
	Name      string `json:"name" binding:"required"`
	Type      string `json:"type,omitempty"`
	Banner    string `json:"banner,omitempty"`
	IsInitial bool   `json:"is_initial,omitempty"`
}

// UpdateBookRequest 更新账本请求
type UpdateBookRequest struct {
	Name   *string `json:"name,omitempty"`
	Type   *string `json:"type,omitempty"`
	Banner *string `json:"banner,omitempty"`
}

// BookQueryFilter 账本查询过滤器
type BookQueryFilter struct {
	UserID string `json:"-"`
	QueryParams
}

// BookRepository 账本数据访问接口
type BookRepository interface {
	Create(c context.Context, book *Book) error
	GetByID(c context.Context, id string) (*Book, error)
	ListByUser(c context.Context, userID string) ([]*Book, error)
	Update(c context.Context, book *Book) error
	Delete(c context.Context, id string) error
	AddMember(c context.Context, bookID, userID string) error
	RemoveMember(c context.Context, bookID, userID string) error
}

// BookUseCase 账本业务逻辑接口
type BookUseCase interface {
	CreateBook(c context.Context, userID string, req *CreateBookRequest) (*Book, error)
	GetBook(c context.Context, id string) (*Book, error)
	ListBooks(c context.Context, userID string) ([]*Book, error)
	UpdateBook(c context.Context, id string, req *UpdateBookRequest) (*Book, error)
	DeleteBook(c context.Context, id string) error
	JoinBook(c context.Context, bookID, userID string) error
	ShareBook(c context.Context, bookID string) (string, error)
}
