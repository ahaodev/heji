package repository

import (
	"context"
	"fmt"
	"shadmin/domain"
	"shadmin/ent"
	"shadmin/ent/bill"
	"shadmin/ent/book"
	"shadmin/ent/user"
	"time"
)

type entSyncRepository struct {
	client *ent.Client
}

// NewSyncRepository creates a new sync repository
func NewSyncRepository(client *ent.Client) domain.SyncRepository {
	return &entSyncRepository{client: client}
}

func (r *entSyncRepository) QueryChanges(c context.Context, userID string, sinceMs int64, limit int) (*domain.SyncChangesResponse, error) {
	sinceTime := time.UnixMilli(sinceMs)

	// 查询用户所属账本（含已删除）中 updated_at > since 的变更
	books, err := r.client.Book.Query().
		Where(
			book.HasMembersWith(user.ID(userID)),
			book.UpdatedAtGT(sinceTime),
		).
		WithMembers().
		Order(ent.Asc(book.FieldUpdatedAt)).
		Limit(limit).
		All(c)
	if err != nil {
		return nil, fmt.Errorf("failed to query book changes: %w", err)
	}

	// 获取用户所属账本 ID 列表（需要查所有账本包括已删除的来获取其账单变更）
	bookIDs, err := r.client.Book.Query().
		Where(book.HasMembersWith(user.ID(userID))).
		IDs(c)
	if err != nil {
		return nil, fmt.Errorf("failed to query user book IDs: %w", err)
	}

	// 查询这些账本中 updated_at > since 的账单变更
	bills, err := r.client.Bill.Query().
		Where(
			bill.BookIDIn(bookIDs...),
			bill.UpdatedAtGT(sinceTime),
		).
		Order(ent.Asc(bill.FieldUpdatedAt)).
		Limit(limit).
		All(c)
	if err != nil {
		return nil, fmt.Errorf("failed to query bill changes: %w", err)
	}

	// 转换为 domain 对象
	domainBooks := make([]*domain.Book, len(books))
	for i, b := range books {
		domainBooks[i] = entBookToDomain(b)
	}

	domainBills := make([]*domain.Bill, len(bills))
	for i, b := range bills {
		domainBills[i] = entBillToDomain(b)
	}

	// 判断是否有更多数据
	hasMore := len(books) >= limit || len(bills) >= limit

	// 计算 next_since：取最新 updated_at 的毫秒时间戳
	var nextSince int64
	if len(domainBooks) > 0 {
		t := domainBooks[len(domainBooks)-1].UpdatedAt.UnixMilli()
		if t > nextSince {
			nextSince = t
		}
	}
	if len(domainBills) > 0 {
		t := domainBills[len(domainBills)-1].UpdatedAt.UnixMilli()
		if t > nextSince {
			nextSince = t
		}
	}
	if nextSince == 0 {
		nextSince = sinceMs
	}

	return &domain.SyncChangesResponse{
		Books:     domainBooks,
		Bills:     domainBills,
		HasMore:   hasMore,
		NextSince: nextSince,
	}, nil
}
