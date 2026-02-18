package usecase

import (
	"context"
	"fmt"
	"shadmin/domain"
	"time"
)

type billImageUsecase struct {
	imageRepo domain.BillImageRepository
	fileRepo  domain.FileRepository
	timeout   time.Duration
}

// NewBillImageUsecase creates a new bill image usecase
func NewBillImageUsecase(imageRepo domain.BillImageRepository, fileRepo domain.FileRepository, timeout time.Duration) domain.BillImageUseCase {
	return &billImageUsecase{imageRepo: imageRepo, fileRepo: fileRepo, timeout: timeout}
}

func (uc *billImageUsecase) UploadImage(c context.Context, billID string, img *domain.BillImage, upload *domain.UploadRequest) (*domain.BillImage, error) {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()

	result, err := uc.fileRepo.Upload(ctx, upload)
	if err != nil {
		return nil, fmt.Errorf("failed to upload image file: %w", err)
	}

	img.BillID = billID
	img.Key = result.Key
	img.Size = result.Size
	img.MD5 = result.ETag

	if err := uc.imageRepo.Create(ctx, img); err != nil {
		return nil, fmt.Errorf("failed to save image record: %w", err)
	}
	return img, nil
}

func (uc *billImageUsecase) GetImage(c context.Context, id string) (*domain.BillImage, error) {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()
	return uc.imageRepo.GetByID(ctx, id)
}

func (uc *billImageUsecase) ListByBillID(c context.Context, billID string) ([]*domain.BillImage, error) {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()
	return uc.imageRepo.ListByBillID(ctx, billID)
}

func (uc *billImageUsecase) DeleteImage(c context.Context, billID, imageID string) error {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()

	img, err := uc.imageRepo.GetByID(ctx, imageID)
	if err != nil {
		return fmt.Errorf("failed to get image: %w", err)
	}

	if img.BillID != billID {
		return fmt.Errorf("image does not belong to the specified bill")
	}

	// 删除文件存储中的文件
	if img.Key != "" {
		_ = uc.fileRepo.Delete(ctx, "heji", img.Key)
	}

	return uc.imageRepo.Delete(ctx, imageID)
}
