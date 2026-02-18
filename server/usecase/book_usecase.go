package usecase

import (
	"context"
	"fmt"
	"shadmin/domain"
	"time"
)

type bookUsecase struct {
	bookRepo domain.BookRepository
	timeout  time.Duration
}

// NewBookUsecase creates a new book usecase
func NewBookUsecase(bookRepo domain.BookRepository, timeout time.Duration) domain.BookUseCase {
	return &bookUsecase{bookRepo: bookRepo, timeout: timeout}
}

func (uc *bookUsecase) CreateBook(c context.Context, userID string, req *domain.CreateBookRequest) (*domain.Book, error) {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()

	book := &domain.Book{
		ID:        req.ID,
		Name:      req.Name,
		Type:      req.Type,
		Banner:    req.Banner,
		CrtUserID: userID,
		IsInitial: req.IsInitial,
	}

	if err := uc.bookRepo.Create(ctx, book); err != nil {
		return nil, fmt.Errorf("failed to create book: %w", err)
	}
	return book, nil
}

func (uc *bookUsecase) GetBook(c context.Context, id string) (*domain.Book, error) {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()
	return uc.bookRepo.GetByID(ctx, id)
}

func (uc *bookUsecase) ListBooks(c context.Context, userID string) ([]*domain.Book, error) {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()
	return uc.bookRepo.ListByUser(ctx, userID)
}

func (uc *bookUsecase) UpdateBook(c context.Context, id string, req *domain.UpdateBookRequest) (*domain.Book, error) {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()

	book, err := uc.bookRepo.GetByID(ctx, id)
	if err != nil {
		return nil, err
	}

	if req.Name != nil {
		book.Name = *req.Name
	}
	if req.Type != nil {
		book.Type = *req.Type
	}
	if req.Banner != nil {
		book.Banner = *req.Banner
	}

	if err := uc.bookRepo.Update(ctx, book); err != nil {
		return nil, fmt.Errorf("failed to update book: %w", err)
	}
	return book, nil
}

func (uc *bookUsecase) DeleteBook(c context.Context, id string) error {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()
	return uc.bookRepo.Delete(ctx, id)
}

func (uc *bookUsecase) JoinBook(c context.Context, bookID, userID string) error {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()
	return uc.bookRepo.AddMember(ctx, bookID, userID)
}

func (uc *bookUsecase) ShareBook(c context.Context, bookID string) (string, error) {
	ctx, cancel := context.WithTimeout(c, uc.timeout)
	defer cancel()

	// 验证账本存在
	_, err := uc.bookRepo.GetByID(ctx, bookID)
	if err != nil {
		return "", fmt.Errorf("book not found: %w", err)
	}
	// 返回账本ID作为分享码
	return bookID, nil
}
