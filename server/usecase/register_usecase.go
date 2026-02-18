package usecase

import (
	"context"
	"fmt"
	"time"

	"shadmin/domain"

	"golang.org/x/crypto/bcrypt"
)

type registerUsecase struct {
	userRepository domain.UserRepository
	contextTimeout time.Duration
}

func NewRegisterUsecase(userRepository domain.UserRepository, timeout time.Duration) domain.RegisterUsecase {
	return &registerUsecase{
		userRepository: userRepository,
		contextTimeout: timeout,
	}
}

func (ru *registerUsecase) CreateUser(c context.Context, user *domain.User) error {
	ctx, cancel := context.WithTimeout(c, ru.contextTimeout)
	defer cancel()

	// 加密密码
	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(user.Password), bcrypt.DefaultCost)
	if err != nil {
		return fmt.Errorf("failed to hash password: %w", err)
	}
	user.Password = string(hashedPassword)

	return ru.userRepository.Create(ctx, user)
}

func (ru *registerUsecase) GetUserByUserName(c context.Context, name string) (*domain.User, error) {
	ctx, cancel := context.WithTimeout(c, ru.contextTimeout)
	defer cancel()
	return ru.userRepository.GetByUsername(ctx, name)
}
