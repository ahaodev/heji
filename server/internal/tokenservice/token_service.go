package tokenservice

import (
	"shadmin/domain"
	"shadmin/internal/tokenutil"
)

// TokenService 认证令牌服务
type TokenService struct{}

// NewTokenService 创建新的令牌服务实例
func NewTokenService() *TokenService {
	return &TokenService{}
}

// CreateAccessToken 创建访问令牌
func (ts *TokenService) CreateAccessToken(user *domain.User, secret string, expiry int) (string, error) {
	return tokenutil.CreateAccessToken(user, secret, expiry)
}

// CreateRefreshToken 创建刷新令牌
func (ts *TokenService) CreateRefreshToken(user *domain.User, secret string, expiry int) (string, error) {
	return tokenutil.CreateRefreshToken(user, secret, expiry)
}

// ExtractIDFromToken 从令牌中提取用户ID
func (ts *TokenService) ExtractIDFromToken(requestToken string, secret string) (string, error) {
	return tokenutil.ExtractIDFromToken(requestToken, secret)
}

// IsAuthorized 验证令牌是否有效
func (ts *TokenService) IsAuthorized(requestToken string, secret string) (bool, error) {
	return tokenutil.IsAuthorized(requestToken, secret)
}

// ExtractEmailFromToken 从令牌中提取用户邮箱
func (ts *TokenService) ExtractEmailFromToken(requestToken string, secret string) (string, error) {
	return tokenutil.ExtractEmailFromToken(requestToken, secret)
}

// ExtractIsAdminFromToken 从令牌中提取用户管理员状态
func (ts *TokenService) ExtractIsAdminFromToken(requestToken string, secret string) (bool, error) {
	return tokenutil.ExtractIsAdminFromToken(requestToken, secret)
}

// ExtractAllClaimsFromToken 从令牌中提取所有自定义claims
func (ts *TokenService) ExtractAllClaimsFromToken(requestToken string, secret string) (*domain.JwtCustomClaims, error) {
	return tokenutil.ExtractAllClaimsFromToken(requestToken, secret)
}
