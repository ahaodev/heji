package domain

import (
	"github.com/golang-jwt/jwt/v5"
)

type JwtCustomClaims struct {
	Name    string   `json:"name"`
	ID      string   `json:"id"`
	Email   string   `json:"email"`
	IsAdmin bool     `json:"is_admin"`
	Roles   []string `json:"roles"`
	jwt.RegisteredClaims
}

type JwtCustomRefreshClaims struct {
	ID string `json:"id"`
	jwt.RegisteredClaims
}
