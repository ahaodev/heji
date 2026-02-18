package repository

import (
	"context"
	"fmt"
	"shadmin/domain"
	"shadmin/ent"
	"shadmin/ent/category"
)

type entCategoryRepository struct {
	client *ent.Client
}

// NewCategoryRepository creates a new category repository
func NewCategoryRepository(client *ent.Client) domain.CategoryRepository {
	return &entCategoryRepository{client: client}
}

func (r *entCategoryRepository) Create(c context.Context, cat *domain.Category) error {
	builder := r.client.Category.Create().
		SetBookID(cat.BookID).
		SetName(cat.Name).
		SetType(cat.Type).
		SetLevel(cat.Level).
		SetIndex(cat.Index)

	if cat.ID != "" {
		builder.SetID(cat.ID)
	}
	if cat.ParentID != "" {
		builder.SetParentID(cat.ParentID)
	}
	if cat.CreateUser != "" {
		builder.SetCreateUser(cat.CreateUser)
	}

	created, err := builder.Save(c)
	if err != nil {
		return fmt.Errorf("failed to create category: %w", err)
	}

	cat.ID = created.ID
	return nil
}

func (r *entCategoryRepository) GetByID(c context.Context, id string) (*domain.Category, error) {
	cat, err := r.client.Category.Get(c, id)
	if err != nil {
		return nil, fmt.Errorf("failed to get category: %w", err)
	}
	return entCategoryToDomain(cat), nil
}

func (r *entCategoryRepository) ListByBookID(c context.Context, bookID string) ([]*domain.Category, error) {
	cats, err := r.client.Category.Query().
		Where(category.BookID(bookID)).
		Order(ent.Asc(category.FieldIndex)).
		All(c)
	if err != nil {
		return nil, fmt.Errorf("failed to list categories: %w", err)
	}

	result := make([]*domain.Category, len(cats))
	for i, cat := range cats {
		result[i] = entCategoryToDomain(cat)
	}
	return result, nil
}

func (r *entCategoryRepository) Update(c context.Context, cat *domain.Category) error {
	builder := r.client.Category.UpdateOneID(cat.ID)

	if cat.Name != "" {
		builder.SetName(cat.Name)
	}
	if cat.Type != 0 {
		builder.SetType(cat.Type)
	}
	builder.SetLevel(cat.Level)
	builder.SetIndex(cat.Index)
	if cat.ParentID != "" {
		builder.SetParentID(cat.ParentID)
	}

	_, err := builder.Save(c)
	if err != nil {
		return fmt.Errorf("failed to update category: %w", err)
	}
	return nil
}

func (r *entCategoryRepository) Delete(c context.Context, id string) error {
	err := r.client.Category.DeleteOneID(id).Exec(c)
	if err != nil {
		return fmt.Errorf("failed to delete category: %w", err)
	}
	return nil
}

func entCategoryToDomain(cat *ent.Category) *domain.Category {
	return &domain.Category{
		ID:         cat.ID,
		BookID:     cat.BookID,
		Name:       cat.Name,
		Type:       cat.Type,
		Level:      cat.Level,
		Index:      cat.Index,
		ParentID:   cat.ParentID,
		CreateUser: cat.CreateUser,
	}
}
