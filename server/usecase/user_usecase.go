package usecase

import (
	"context"
	"errors"
	"fmt"
	"heji-server/domain"
	"heji-server/internal/get"
	"heji-server/internal/tokenutil"
	"heji-server/pkg"
	"time"

	"golang.org/x/crypto/bcrypt"
)

type userUserCase struct {
	userRepository domain.UserRepository
	contextTimeout time.Duration
}

// Login 用户用例实现Login
func (uc *userUserCase) Login(c context.Context, request *domain.LoginRequest) (string, error) {
	user, err := uc.userRepository.GetByTel(c, request.Tel) //用例持有Users用户存储库
	if err != nil {
		return "", errors.New("用户不存在")
	}
	if err := bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(request.Password)); err != nil {
		return "", errors.New("密码不正确")
	}
	jwt := get.Config().Jwt
	token, err := uc.CreateAccessToken(&user, jwt.Secret, jwt.ExpirationTime)
	return token, err
}

func (uc *userUserCase) Register(c context.Context, user *domain.User) error {
	hashedPassword, err := bcrypt.GenerateFromPassword([]byte(user.Password), 12)
	if err != nil {
		return fmt.Errorf("failed to hash password: %w", err)
	}
	user.Password = string(hashedPassword)
	return uc.userRepository.Register(c, user)
}

func NewLoginUseCase(userRepository domain.UserRepository, timeout time.Duration) domain.UserUseCase {
	return &userUserCase{
		userRepository: userRepository,
		contextTimeout: timeout,
	}
}

func (uc *userUserCase) GetByTel(c context.Context, tel string) (domain.User, error) {
	user, err := uc.userRepository.GetByTel(c, tel)
	return user, err
}

func (uc *userUserCase) CreateAccessToken(user *domain.User, secret string, expiry time.Duration) (accessToken string, err error) {
	token, err := tokenutil.CreateAccessToken(user, secret, expiry)
	redis := get.Config().Redis
	err = pkg.InitializeRedisClient(redis.Address, redis.Password, 1)
	if err != nil {
		pkg.Log.Error(err)
	}
	defer pkg.CloseRedisConnection()
	err = pkg.StoreJWTToken(user.Name, token, expiry)
	if err != nil {
		pkg.Log.Error(err)
	}
	return token, err
}

func (uc *userUserCase) CreateRefreshToken(user *domain.User, secret string, expiry time.Duration) (refreshToken string, err error) {
	return tokenutil.CreateRefreshToken(user, secret, expiry)
}
