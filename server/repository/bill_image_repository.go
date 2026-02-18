package repository

import (
	"context"
	"fmt"
	"shadmin/domain"
	"shadmin/ent"
	"shadmin/ent/billimage"
)

type entBillImageRepository struct {
	client *ent.Client
}

// NewBillImageRepository creates a new bill image repository
func NewBillImageRepository(client *ent.Client) domain.BillImageRepository {
	return &entBillImageRepository{client: client}
}

func (r *entBillImageRepository) Create(c context.Context, img *domain.BillImage) error {
	builder := r.client.BillImage.Create().
		SetBillID(img.BillID).
		SetKey(img.Key)

	if img.ID != "" {
		builder.SetID(img.ID)
	}
	if img.Ext != "" {
		builder.SetExt(img.Ext)
	}
	if img.Size > 0 {
		builder.SetSize(img.Size)
	}
	if img.MD5 != "" {
		builder.SetMd5(img.MD5)
	}

	created, err := builder.Save(c)
	if err != nil {
		return fmt.Errorf("failed to create bill image: %w", err)
	}

	img.ID = created.ID
	return nil
}

func (r *entBillImageRepository) GetByID(c context.Context, id string) (*domain.BillImage, error) {
	img, err := r.client.BillImage.Get(c, id)
	if err != nil {
		return nil, fmt.Errorf("failed to get bill image: %w", err)
	}
	return entBillImageToDomain(img), nil
}

func (r *entBillImageRepository) ListByBillID(c context.Context, billID string) ([]*domain.BillImage, error) {
	imgs, err := r.client.BillImage.Query().
		Where(billimage.BillID(billID)).
		All(c)
	if err != nil {
		return nil, fmt.Errorf("failed to list bill images: %w", err)
	}

	result := make([]*domain.BillImage, len(imgs))
	for i, img := range imgs {
		result[i] = entBillImageToDomain(img)
	}
	return result, nil
}

func (r *entBillImageRepository) Delete(c context.Context, id string) error {
	err := r.client.BillImage.DeleteOneID(id).Exec(c)
	if err != nil {
		return fmt.Errorf("failed to delete bill image: %w", err)
	}
	return nil
}

func (r *entBillImageRepository) DeleteByBillID(c context.Context, billID string) error {
	_, err := r.client.BillImage.Delete().
		Where(billimage.BillID(billID)).
		Exec(c)
	if err != nil {
		return fmt.Errorf("failed to delete bill images: %w", err)
	}
	return nil
}

func entBillImageToDomain(img *ent.BillImage) *domain.BillImage {
	return &domain.BillImage{
		ID:     img.ID,
		BillID: img.BillID,
		Key:    img.Key,
		Ext:    img.Ext,
		Size:   img.Size,
		MD5:    img.Md5,
	}
}
