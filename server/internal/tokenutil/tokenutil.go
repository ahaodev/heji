package tokenutil

import (
	"fmt"
	"time"

	"shadmin/domain"

	jwt "github.com/golang-jwt/jwt/v5"
)

func CreateAccessToken(user *domain.User, secret string, expiry int) (accessToken string, err error) {
	exp := jwt.NewNumericDate(time.Now().Add(time.Hour * time.Duration(expiry)))

	claims := &domain.JwtCustomClaims{
		Name:    user.Username,
		ID:      user.ID,
		Email:   user.Email,
		IsAdmin: user.IsAdmin,
		Roles:   user.Roles,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: exp,
		},
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	t, err := token.SignedString([]byte(secret))
	if err != nil {
		return "", err
	}
	return t, err
}

func CreateRefreshToken(user *domain.User, secret string, expiry int) (refreshToken string, err error) {
	exp := jwt.NewNumericDate(time.Now().Add(time.Hour * time.Duration(expiry)))
	claimsRefresh := &domain.JwtCustomRefreshClaims{
		ID: user.ID,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: exp,
		},
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claimsRefresh)
	rt, err := token.SignedString([]byte(secret))
	if err != nil {
		return "", err
	}
	return rt, err
}

func IsAuthorized(requestToken string, secret string) (bool, error) {
	_, err := jwt.Parse(requestToken, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("Unexpected signing method: %v", token.Header["alg"])
		}
		return []byte(secret), nil
	})
	if err != nil {
		return false, err
	}
	return true, nil
}

func ExtractIDFromToken(requestToken string, secret string) (string, error) {
	token, err := jwt.Parse(requestToken, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("Unexpected signing method: %v", token.Header["alg"])
		}
		return []byte(secret), nil
	})

	if err != nil {
		return "", err
	}

	claims, ok := token.Claims.(jwt.MapClaims)

	if !ok && !token.Valid {
		return "", fmt.Errorf("invalid token")
	}

	return claims["id"].(string), nil
}

// ExtractEmailFromToken 从token中提取用户邮箱
func ExtractEmailFromToken(requestToken string, secret string) (string, error) {
	token, err := jwt.Parse(requestToken, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("Unexpected signing method: %v", token.Header["alg"])
		}
		return []byte(secret), nil
	})

	if err != nil {
		return "", err
	}

	claims, ok := token.Claims.(jwt.MapClaims)
	if !ok && !token.Valid {
		return "", fmt.Errorf("invalid token")
	}

	email, exists := claims["email"]
	if !exists {
		return "", nil // 邮箱不存在时返回空字符串
	}

	emailStr, ok := email.(string)
	if !ok {
		return "", fmt.Errorf("email claim is not a string")
	}

	return emailStr, nil
}

// ExtractIsAdminFromToken 从token中提取用户是否为管理员
func ExtractIsAdminFromToken(requestToken string, secret string) (bool, error) {
	token, err := jwt.Parse(requestToken, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("Unexpected signing method: %v", token.Header["alg"])
		}
		return []byte(secret), nil
	})

	if err != nil {
		return false, err
	}

	claims, ok := token.Claims.(jwt.MapClaims)
	if !ok && !token.Valid {
		return false, fmt.Errorf("invalid token")
	}

	isAdmin, exists := claims["is_admin"]
	if !exists {
		return false, nil // is_admin不存在时返回false
	}

	isAdminBool, ok := isAdmin.(bool)
	if !ok {
		return false, fmt.Errorf("is_admin claim is not a boolean")
	}

	return isAdminBool, nil
}

// ExtractAllClaimsFromToken 从token中提取所有自定义claims
func ExtractAllClaimsFromToken(requestToken string, secret string) (*domain.JwtCustomClaims, error) {
	token, err := jwt.Parse(requestToken, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("Unexpected signing method: %v", token.Header["alg"])
		}
		return []byte(secret), nil
	})

	if err != nil {
		return nil, err
	}

	claims, ok := token.Claims.(jwt.MapClaims)
	if !ok && !token.Valid {
		return nil, fmt.Errorf("invalid token")
	}

	// 安全地提取各个字段
	customClaims := &domain.JwtCustomClaims{}

	if id, exists := claims["id"]; exists {
		if idStr, ok := id.(string); ok {
			customClaims.ID = idStr
		}
	}

	if name, exists := claims["name"]; exists {
		if nameStr, ok := name.(string); ok {
			customClaims.Name = nameStr
		}
	}

	if email, exists := claims["email"]; exists {
		if emailStr, ok := email.(string); ok {
			customClaims.Email = emailStr
		}
	}

	if isAdmin, exists := claims["is_admin"]; exists {
		if isAdminBool, ok := isAdmin.(bool); ok {
			customClaims.IsAdmin = isAdminBool
		}
	}

	if roles, exists := claims["roles"]; exists {
		if rolesSlice, ok := roles.([]interface{}); ok {
			customClaims.Roles = make([]string, len(rolesSlice))
			for i, role := range rolesSlice {
				if roleStr, ok := role.(string); ok {
					customClaims.Roles[i] = roleStr
				}
			}
		}
	}
	return customClaims, nil
}

func ExtractIDRoleFromToken(requestToken string, secret string) (string, error) {
	token, err := jwt.Parse(requestToken, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("Unexpected signing method: %v", token.Header["alg"])
		}
		return []byte(secret), nil
	})

	if err != nil {
		return "", err
	}

	claims, ok := token.Claims.(jwt.MapClaims)

	if !ok && !token.Valid {
		return "", fmt.Errorf("Invalid Token")
	}

	return claims["id"].(string), nil
}
