package domain

import (
	"context"
)

// BillImage 账单图片实体
type BillImage struct {
	ID     string `json:"_id"`
	BillID string `json:"bill_id"`
	Key    string `json:"key"`
	Ext    string `json:"ext,omitempty"`
	Size   int64  `json:"length,omitempty"`
	MD5    string `json:"md5,omitempty"`
}

// BillImageRepository 账单图片数据访问接口
type BillImageRepository interface {
	Create(c context.Context, img *BillImage) error
	GetByID(c context.Context, id string) (*BillImage, error)
	ListByBillID(c context.Context, billID string) ([]*BillImage, error)
	Delete(c context.Context, id string) error
	DeleteByBillID(c context.Context, billID string) error
}

// BillImageUseCase 账单图片业务逻辑接口
type BillImageUseCase interface {
	UploadImage(c context.Context, billID string, img *BillImage, upload *UploadRequest) (*BillImage, error)
	GetImage(c context.Context, id string) (*BillImage, error)
	ListByBillID(c context.Context, billID string) ([]*BillImage, error)
	DeleteImage(c context.Context, billID, imageID string) error
}
