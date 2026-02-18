package usecase

import (
	"context"
	"fmt"
	"shadmin/domain"
	"time"
)

type categoryUsecase struct {
	categoryRepo domain.CategoryRepository
	timeout      time.Duration
}

// NewCategoryUsecase creates a new category usecase
func NewCategoryUsecase(categoryRepo domain.CategoryRepository, timeout time.Duration) domain.CategoryUseCase {
	return &categoryUsecase{categoryRepo: categoryRepo, timeout: timeout}
}

func (uc *categoryUsecase) CreateCategory(c context.Context, userID string, req *domain.CreateCategoryRequest) (*domain.Category, error) {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()

	cat := &domain.Category{
		ID:         req.ID,
		BookID:     req.BookID,
		Name:       req.Name,
		Type:       req.Type,
		Level:      req.Level,
		Index:      req.Index,
		ParentID:   req.ParentID,
		CreateUser: userID,
	}

	if err := uc.categoryRepo.Create(ctx, cat); err != nil {
		return nil, fmt.Errorf("failed to create category: %w", err)
	}
	return cat, nil
}

func (uc *categoryUsecase) CreateCategories(c context.Context, userID string, reqs []*domain.CreateCategoryRequest) ([]*domain.Category, error) {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()

	result := make([]*domain.Category, 0, len(reqs))
	for _, req := range reqs {
		cat := &domain.Category{
			ID:         req.ID,
			BookID:     req.BookID,
			Name:       req.Name,
			Type:       req.Type,
			Level:      req.Level,
			Index:      req.Index,
			ParentID:   req.ParentID,
			CreateUser: userID,
		}
		if err := uc.categoryRepo.Create(ctx, cat); err != nil {
			return nil, fmt.Errorf("failed to create category %s: %w", req.Name, err)
		}
		result = append(result, cat)
	}
	return result, nil
}

func (uc *categoryUsecase) GetCategory(c context.Context, id string) (*domain.Category, error) {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()
	return uc.categoryRepo.GetByID(ctx, id)
}

func (uc *categoryUsecase) ListCategories(c context.Context, bookID string) ([]*domain.Category, error) {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()
	return uc.categoryRepo.ListByBookID(ctx, bookID)
}

func (uc *categoryUsecase) UpdateCategory(c context.Context, id string, req *domain.UpdateCategoryRequest) (*domain.Category, error) {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()

	cat, err := uc.categoryRepo.GetByID(ctx, id)
	if err != nil {
		return nil, err
	}

	if req.Name != nil {
		cat.Name = *req.Name
	}
	if req.Type != nil {
		cat.Type = *req.Type
	}
	if req.Level != nil {
		cat.Level = *req.Level
	}
	if req.Index != nil {
		cat.Index = *req.Index
	}
	if req.ParentID != nil {
		cat.ParentID = *req.ParentID
	}

	if err := uc.categoryRepo.Update(ctx, cat); err != nil {
		return nil, fmt.Errorf("failed to update category: %w", err)
	}
	return cat, nil
}

func (uc *categoryUsecase) DeleteCategory(c context.Context, id string) error {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()
	return uc.categoryRepo.Delete(ctx, id)
}
