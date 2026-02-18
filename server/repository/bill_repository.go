package repository

import (
	"context"
	"fmt"
	"shadmin/domain"
	"shadmin/ent"
	"shadmin/ent/bill"
	"time"
)

type entBillRepository struct {
	client *ent.Client
}

// NewBillRepository creates a new bill repository
func NewBillRepository(client *ent.Client) domain.BillRepository {
	return &entBillRepository{client: client}
}

func (r *entBillRepository) Create(c context.Context, b *domain.Bill) error {
	// Upsert: if bill with same ID exists, update it instead
	if b.ID != "" {
		existing, err := r.client.Bill.Get(c, b.ID)
		if err == nil {
			// Bill exists — update it
			builder := r.client.Bill.UpdateOneID(existing.ID).
				SetBookID(b.BookID).
				SetMoney(b.Money).
				SetType(b.Type).
				SetTime(b.Time)
			if b.Category != "" {
				builder.SetCategory(b.Category)
			}
			if b.Remark != "" {
				builder.SetRemark(b.Remark)
			}
			if b.Images != nil {
				builder.SetImages(b.Images)
			}
			updated, err := builder.Save(c)
			if err != nil {
				return fmt.Errorf("failed to update existing bill: %w", err)
			}
			b.CreatedAt = updated.CreatedAt
			b.UpdatedAt = updated.UpdatedAt
			return nil
		}
	}

	builder := r.client.Bill.Create().
		SetBookID(b.BookID).
		SetMoney(b.Money).
		SetType(b.Type).
		SetCrtUser(b.CrtUser).
		SetTime(b.Time)

	if b.ID != "" {
		builder.SetID(b.ID)
	}
	if b.Category != "" {
		builder.SetCategory(b.Category)
	}
	if b.Remark != "" {
		builder.SetRemark(b.Remark)
	}
	if len(b.Images) > 0 {
		builder.SetImages(b.Images)
	}

	created, err := builder.Save(c)
	if err != nil {
		return fmt.Errorf("failed to create bill: %w", err)
	}

	b.ID = created.ID
	b.CreatedAt = created.CreatedAt
	b.UpdatedAt = created.UpdatedAt
	return nil
}

func (r *entBillRepository) GetByID(c context.Context, id string) (*domain.Bill, error) {
	b, err := r.client.Bill.Get(c, id)
	if err != nil {
		return nil, fmt.Errorf("failed to get bill: %w", err)
	}
	return entBillToDomain(b), nil
}

func (r *entBillRepository) Query(c context.Context, filter domain.BillQueryFilter) (*domain.PagedResult[*domain.Bill], error) {
	query := r.client.Bill.Query().
		Where(bill.DeletedAtIsNil())

	if filter.BookID != "" {
		query = query.Where(bill.BookID(filter.BookID))
	}
	if filter.Type != nil {
		query = query.Where(bill.TypeEQ(*filter.Type))
	}
	if filter.Category != "" {
		query = query.Where(bill.Category(filter.Category))
	}
	if filter.TimeFrom != "" {
		query = query.Where(bill.TimeGTE(filter.TimeFrom))
	}
	if filter.TimeTo != "" {
		query = query.Where(bill.TimeLTE(filter.TimeTo))
	}

	total, err := query.Clone().Count(c)
	if err != nil {
		return nil, fmt.Errorf("failed to count bills: %w", err)
	}

	page := filter.GetPage()
	pageSize := filter.GetPageSize()
	offset := (page - 1) * pageSize

	bills, err := query.
		Offset(offset).
		Limit(pageSize).
		Order(ent.Desc(bill.FieldTime)).
		All(c)
	if err != nil {
		return nil, fmt.Errorf("failed to query bills: %w", err)
	}

	items := make([]*domain.Bill, len(bills))
	for i, b := range bills {
		items[i] = entBillToDomain(b)
	}

	return domain.NewPagedResult(items, total, page, pageSize), nil
}

func (r *entBillRepository) Update(c context.Context, b *domain.Bill) error {
	builder := r.client.Bill.UpdateOneID(b.ID)

	if b.Money != 0 {
		builder.SetMoney(b.Money)
	}
	if b.Type != 0 {
		builder.SetType(b.Type)
	}
	if b.Category != "" {
		builder.SetCategory(b.Category)
	}
	if b.Time != "" {
		builder.SetTime(b.Time)
	}
	if b.Remark != "" {
		builder.SetRemark(b.Remark)
	}
	if b.Images != nil {
		builder.SetImages(b.Images)
	}

	updated, err := builder.Save(c)
	if err != nil {
		return fmt.Errorf("failed to update bill: %w", err)
	}
	b.UpdatedAt = updated.UpdatedAt
	return nil
}

func (r *entBillRepository) Delete(c context.Context, id string) error {
	now := time.Now()
	_, err := r.client.Bill.UpdateOneID(id).
		SetDeletedAt(now).
		Save(c)
	if err != nil {
		return fmt.Errorf("failed to soft delete bill: %w", err)
	}
	return nil
}

func entBillToDomain(b *ent.Bill) *domain.Bill {
	return &domain.Bill{
		ID:        b.ID,
		BookID:    b.BookID,
		Money:     b.Money,
		Type:      b.Type,
		Category:  b.Category,
		CrtUser:   b.CrtUser,
		Time:      b.Time,
		Remark:    b.Remark,
		Images:    b.Images,
		DeletedAt: b.DeletedAt,
		CreatedAt: b.CreatedAt,
		UpdatedAt: b.UpdatedAt,
	}
}
