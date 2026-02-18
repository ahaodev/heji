package repository

import (
	"context"
	"fmt"
	"shadmin/domain"
	"shadmin/ent"
	"shadmin/ent/book"
	"shadmin/ent/user"
	"time"
)

type entBookRepository struct {
	client *ent.Client
}

// NewBookRepository creates a new book repository
func NewBookRepository(client *ent.Client) domain.BookRepository {
	return &entBookRepository{client: client}
}

func (r *entBookRepository) Create(c context.Context, b *domain.Book) error {
	// Upsert: if book with same ID exists, update it instead
	if b.ID != "" {
		existing, err := r.client.Book.Get(c, b.ID)
		if err == nil {
			// Book exists — update it
			builder := r.client.Book.UpdateOneID(existing.ID).
				SetName(b.Name)
			if b.Type != "" {
				builder.SetType(b.Type)
			}
			if b.Banner != "" {
				builder.SetBanner(b.Banner)
			}
			updated, err := builder.Save(c)
			if err != nil {
				return fmt.Errorf("failed to update existing book: %w", err)
			}
			b.CreatedAt = updated.CreatedAt
			b.UpdatedAt = updated.UpdatedAt
			return nil
		}
	}

	builder := r.client.Book.Create().
		SetName(b.Name).
		SetCrtUserID(b.CrtUserID).
		SetIsInitial(b.IsInitial)

	if b.ID != "" {
		builder.SetID(b.ID)
	}
	if b.Type != "" {
		builder.SetType(b.Type)
	}
	if b.Banner != "" {
		builder.SetBanner(b.Banner)
	}

	created, err := builder.Save(c)
	if err != nil {
		return fmt.Errorf("failed to create book: %w", err)
	}

	b.ID = created.ID
	b.CreatedAt = created.CreatedAt
	b.UpdatedAt = created.UpdatedAt

	// 将创建者添加为成员
	_, err = r.client.Book.UpdateOneID(created.ID).
		AddMemberIDs(b.CrtUserID).
		Save(c)
	if err != nil {
		return fmt.Errorf("failed to add creator as member: %w", err)
	}

	return nil
}

func (r *entBookRepository) GetByID(c context.Context, id string) (*domain.Book, error) {
	b, err := r.client.Book.Query().
		Where(book.ID(id)).
		WithMembers().
		Only(c)
	if err != nil {
		return nil, fmt.Errorf("failed to get book: %w", err)
	}
	return entBookToDomain(b), nil
}

func (r *entBookRepository) ListByUser(c context.Context, userID string) ([]*domain.Book, error) {
	books, err := r.client.Book.Query().
		Where(
			book.HasMembersWith(user.ID(userID)),
			book.DeletedAtIsNil(),
		).
		WithMembers().
		All(c)
	if err != nil {
		return nil, fmt.Errorf("failed to list books: %w", err)
	}

	result := make([]*domain.Book, len(books))
	for i, b := range books {
		result[i] = entBookToDomain(b)
	}
	return result, nil
}

func (r *entBookRepository) Update(c context.Context, b *domain.Book) error {
	builder := r.client.Book.UpdateOneID(b.ID)
	if b.Name != "" {
		builder.SetName(b.Name)
	}
	if b.Type != "" {
		builder.SetType(b.Type)
	}
	if b.Banner != "" {
		builder.SetBanner(b.Banner)
	}

	updated, err := builder.Save(c)
	if err != nil {
		return fmt.Errorf("failed to update book: %w", err)
	}
	b.UpdatedAt = updated.UpdatedAt
	return nil
}

func (r *entBookRepository) Delete(c context.Context, id string) error {
	now := time.Now()
	_, err := r.client.Book.UpdateOneID(id).
		SetDeletedAt(now).
		Save(c)
	if err != nil {
		return fmt.Errorf("failed to soft delete book: %w", err)
	}
	return nil
}

func (r *entBookRepository) AddMember(c context.Context, bookID, userID string) error {
	_, err := r.client.Book.UpdateOneID(bookID).
		AddMemberIDs(userID).
		Save(c)
	if err != nil {
		return fmt.Errorf("failed to add member: %w", err)
	}
	return nil
}

func (r *entBookRepository) RemoveMember(c context.Context, bookID, userID string) error {
	_, err := r.client.Book.UpdateOneID(bookID).
		RemoveMemberIDs(userID).
		Save(c)
	if err != nil {
		return fmt.Errorf("failed to remove member: %w", err)
	}
	return nil
}

func entBookToDomain(b *ent.Book) *domain.Book {
	members := make([]string, 0)
	if b.Edges.Members != nil {
		for _, m := range b.Edges.Members {
			members = append(members, m.ID)
		}
	}
	return &domain.Book{
		ID:        b.ID,
		Name:      b.Name,
		Type:      b.Type,
		Banner:    b.Banner,
		CrtUserID: b.CrtUserID,
		IsInitial: b.IsInitial,
		Members:   members,
		DeletedAt: b.DeletedAt,
		CreatedAt: b.CreatedAt,
		UpdatedAt: b.UpdatedAt,
	}
}
