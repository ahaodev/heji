package domain

import (
	"context"
)

type RefreshTokenUsecase interface {
	GetUserByID(c context.Context, id string) (*User, error)
	ExtractIDFromToken(requestToken string, secret string) (string, error)
}
