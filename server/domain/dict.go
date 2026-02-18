package domain

import (
	"context"
	"errors"
	"time"
)

var (
	ErrDictTypeCodeExists      = errors.New("dictionary type code already exists")
	ErrDictTypeHasItems        = errors.New("dictionary type has items, cannot delete")
	ErrDictTypeNotFound        = errors.New("dictionary type not found")
	ErrDictItemNotFound        = errors.New("dictionary item not found")
	ErrDictItemValueExists     = errors.New("dictionary item value already exists in this type")
	ErrDictItemDefaultConflict = errors.New("dictionary type already has a default item")
)

// DictType 字典类型
type DictType struct {
	ID        string    `json:"id"`
	Code      string    `json:"code"`   // 唯一标识
	Name      string    `json:"name"`   // 显示名称
	Status    string    `json:"status"` // active, inactive
	Remark    string    `json:"remark,omitempty"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

// DictItem 字典项
type DictItem struct {
	ID        string    `json:"id"`
	TypeID    string    `json:"type_id"`          // 字典类型ID
	Label     string    `json:"label"`            // 显示标签
	Value     string    `json:"value"`            // 实际值
	Sort      int       `json:"sort"`             // 排序，默认0
	IsDefault bool      `json:"is_default"`       // 是否默认项，默认false
	Status    string    `json:"status"`           // active, inactive
	Color     string    `json:"color,omitempty"`  // 颜色标识
	Remark    string    `json:"remark,omitempty"` // 备注
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

// CreateDictTypeRequest 创建字典类型请求
type CreateDictTypeRequest struct {
	Code   string `json:"code" binding:"required"` // 唯一标识
	Name   string `json:"name" binding:"required"` // 显示名称
	Status string `json:"status,omitempty"`        // active, inactive，默认active
	Remark string `json:"remark,omitempty"`        // 备注
}

// UpdateDictTypeRequest 更新字典类型请求
type UpdateDictTypeRequest struct {
	Code   *string `json:"code,omitempty"`
	Name   *string `json:"name,omitempty"`
	Status *string `json:"status,omitempty"`
	Remark *string `json:"remark,omitempty"`
}

// DictTypeQueryParams 字典类型查询参数
type DictTypeQueryParams struct {
	Code   string `json:"code,omitempty" form:"code"`
	Name   string `json:"name,omitempty" form:"name"`
	Status string `json:"status,omitempty" form:"status"`
	Search string `json:"search,omitempty" form:"search"` // 模糊搜索code和name
	QueryParams
}

// CreateDictItemRequest 创建字典项请求
type CreateDictItemRequest struct {
	TypeID    string `json:"type_id" binding:"required"` // 字典类型ID
	Label     string `json:"label" binding:"required"`   // 显示标签
	Value     string `json:"value" binding:"required"`   // 实际值
	Sort      int    `json:"sort,omitempty"`             // 排序，默认0
	IsDefault bool   `json:"is_default,omitempty"`       // 是否默认项
	Status    string `json:"status,omitempty"`           // active, inactive，默认active
	Color     string `json:"color,omitempty"`            // 颜色标识
	Remark    string `json:"remark,omitempty"`           // 备注
}

// UpdateDictItemRequest 更新字典项请求
type UpdateDictItemRequest struct {
	Label     *string `json:"label,omitempty"`
	Value     *string `json:"value,omitempty"`
	Sort      *int    `json:"sort,omitempty"`
	IsDefault *bool   `json:"is_default,omitempty"`
	Status    *string `json:"status,omitempty"`
	Color     *string `json:"color,omitempty"`
	Remark    *string `json:"remark,omitempty"`
}

// DictItemQueryParams 字典项查询参数
type DictItemQueryParams struct {
	TypeID   string `json:"type_id,omitempty" form:"type_id"`     // 字典类型ID
	TypeCode string `json:"type_code,omitempty" form:"type_code"` // 字典类型Code
	Label    string `json:"label,omitempty" form:"label"`
	Value    string `json:"value,omitempty" form:"value"`
	Status   string `json:"status,omitempty" form:"status"`
	Search   string `json:"search,omitempty" form:"search"` // 模糊搜索label和value
	QueryParams
}

// DictTypePagedResult 字典类型分页查询结果
type DictTypePagedResult = PagedResult[*DictType]

// DictItemPagedResult 字典项分页查询结果
type DictItemPagedResult = PagedResult[*DictItem]

// DictRepository 字典数据访问接口
type DictRepository interface {
	// 字典类型相关
	CreateType(ctx context.Context, dictType *DictType) error
	GetTypeByID(ctx context.Context, id string) (*DictType, error)
	GetTypeByCode(ctx context.Context, code string) (*DictType, error)
	FetchTypes(ctx context.Context, params DictTypeQueryParams) (*PagedResult[*DictType], error)
	UpdateType(ctx context.Context, id string, updates UpdateDictTypeRequest) error
	DeleteType(ctx context.Context, id string) error

	// 字典项相关
	CreateItem(ctx context.Context, dictItem *DictItem) error
	GetItemByID(ctx context.Context, id string) (*DictItem, error)
	FetchItems(ctx context.Context, params DictItemQueryParams) (*PagedResult[*DictItem], error)
	UpdateItem(ctx context.Context, id string, updates UpdateDictItemRequest) error
	DeleteItem(ctx context.Context, id string) error

	// 便捷查询
	GetItemsByTypeCode(ctx context.Context, typeCode string, status string) ([]*DictItem, error)
}

// DictUseCase 字典业务逻辑接口
type DictUseCase interface {
	// 字典类型相关
	CreateDictType(ctx context.Context, request *CreateDictTypeRequest) (*DictType, error)
	GetDictTypeByID(ctx context.Context, id string) (*DictType, error)
	GetDictTypeByCode(ctx context.Context, code string) (*DictType, error)
	ListDictTypes(ctx context.Context, params DictTypeQueryParams) (*PagedResult[*DictType], error)
	UpdateDictType(ctx context.Context, id string, updates UpdateDictTypeRequest) error
	DeleteDictType(ctx context.Context, id string) error

	// 字典项相关
	CreateDictItem(ctx context.Context, request *CreateDictItemRequest) (*DictItem, error)
	GetDictItemByID(ctx context.Context, id string) (*DictItem, error)
	ListDictItems(ctx context.Context, params DictItemQueryParams) (*PagedResult[*DictItem], error)
	UpdateDictItem(ctx context.Context, id string, updates UpdateDictItemRequest) error
	DeleteDictItem(ctx context.Context, id string) error

	// 便捷查询
	GetActiveItemsByTypeCode(ctx context.Context, typeCode string) ([]*DictItem, error)
}
