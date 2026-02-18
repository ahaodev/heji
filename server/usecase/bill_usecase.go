package usecase

import (
	"context"
	"fmt"
	"shadmin/domain"
	"time"
)

type billUsecase struct {
	billRepo domain.BillRepository
	timeout  time.Duration
}

// NewBillUsecase creates a new bill usecase
func NewBillUsecase(billRepo domain.BillRepository, timeout time.Duration) domain.BillUseCase {
	return &billUsecase{billRepo: billRepo, timeout: timeout}
}

func (uc *billUsecase) CreateBill(c context.Context, userID string, req *domain.CreateBillRequest) (*domain.Bill, error) {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()

	bill := &domain.Bill{
		ID:       req.ID,
		BookID:   req.BookID,
		Money:    req.Money,
		Type:     req.Type,
		Category: req.Category,
		CrtUser:  userID,
		Time:     req.Time,
		Remark:   req.Remark,
		Images:   req.Images,
	}

	if err := uc.billRepo.Create(ctx, bill); err != nil {
		return nil, fmt.Errorf("failed to create bill: %w", err)
	}
	return bill, nil
}

func (uc *billUsecase) GetBill(c context.Context, id string) (*domain.Bill, error) {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()
	return uc.billRepo.GetByID(ctx, id)
}

func (uc *billUsecase) ListBills(c context.Context, filter domain.BillQueryFilter) (*domain.PagedResult[*domain.Bill], error) {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()
	return uc.billRepo.Query(ctx, filter)
}

func (uc *billUsecase) UpdateBill(c context.Context, id string, req *domain.UpdateBillRequest) (*domain.Bill, error) {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()

	bill, err := uc.billRepo.GetByID(ctx, id)
	if err != nil {
		return nil, err
	}

	if req.Money != nil {
		bill.Money = *req.Money
	}
	if req.Type != nil {
		bill.Type = *req.Type
	}
	if req.Category != nil {
		bill.Category = *req.Category
	}
	if req.Time != nil {
		bill.Time = *req.Time
	}
	if req.Remark != nil {
		bill.Remark = *req.Remark
	}
	if req.Images != nil {
		bill.Images = req.Images
	}

	if err := uc.billRepo.Update(ctx, bill); err != nil {
		return nil, fmt.Errorf("failed to update bill: %w", err)
	}
	return bill, nil
}

func (uc *billUsecase) DeleteBill(c context.Context, id string) error {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()
	return uc.billRepo.Delete(ctx, id)
}
