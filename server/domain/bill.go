package domain

import (
	"context"
	"time"
)

// Bill 账单实体
type Bill struct {
	ID          string     `json:"_id"`
	BookID      string     `json:"book_id"`
	Money       float64    `json:"money"`
	Type        int        `json:"type"`
	Category    string     `json:"category,omitempty"`
	CrtUser     string     `json:"crt_user"`
	CrtUserName string     `json:"crt_user_name,omitempty"`
	Time        string     `json:"time"`
	Remark      string     `json:"remark,omitempty"`
	Images      []string   `json:"images,omitempty"`
	DeletedAt   *time.Time `json:"deleted_at,omitempty"`
	CreatedAt   time.Time  `json:"crt_time"`
	UpdatedAt   time.Time  `json:"upd_time"`
}

// CreateBillRequest 创建账单请求
type CreateBillRequest struct {
	ID       string   `json:"_id,omitempty"`
	BookID   string   `json:"book_id" binding:"required"`
	Money    float64  `json:"money" binding:"required"`
	Type     int      `json:"type" binding:"required"`
	Category string   `json:"category,omitempty"`
	Time     string   `json:"time" binding:"required"`
	Remark   string   `json:"remark,omitempty"`
	Images   []string `json:"images,omitempty"`
}

// UpdateBillRequest 更新账单请求
type UpdateBillRequest struct {
	Money    *float64 `json:"money,omitempty"`
	Type     *int     `json:"type,omitempty"`
	Category *string  `json:"category,omitempty"`
	Time     *string  `json:"time,omitempty"`
	Remark   *string  `json:"remark,omitempty"`
	Images   []string `json:"images,omitempty"`
}

// BillQueryFilter 账单查询过滤器
type BillQueryFilter struct {
	BookID   string `json:"book_id"`
	Type     *int   `json:"type,omitempty"`
	Category string `json:"category,omitempty"`
	TimeFrom string `json:"time_from,omitempty"`
	TimeTo   string `json:"time_to,omitempty"`
	QueryParams
}

// BillRepository 账单数据访问接口
type BillRepository interface {
	Create(c context.Context, bill *Bill) error
	GetByID(c context.Context, id string) (*Bill, error)
	Query(c context.Context, filter BillQueryFilter) (*PagedResult[*Bill], error)
	Update(c context.Context, bill *Bill) error
	Delete(c context.Context, id string) error
}

// BillUseCase 账单业务逻辑接口
type BillUseCase interface {
	CreateBill(c context.Context, userID string, req *CreateBillRequest) (*Bill, error)
	GetBill(c context.Context, id string) (*Bill, error)
	ListBills(c context.Context, filter BillQueryFilter) (*PagedResult[*Bill], error)
	UpdateBill(c context.Context, id string, req *UpdateBillRequest) (*Bill, error)
	DeleteBill(c context.Context, id string) error
}
