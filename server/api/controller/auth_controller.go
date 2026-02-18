package controller

import (
	"fmt"
	"net"
	"net/http"
	bootstrap "shadmin/bootstrap"
	"shadmin/internal/casbin"
	"strings"

	"golang.org/x/crypto/bcrypt"

	"shadmin/domain"
	"shadmin/internal"
	"shadmin/internal/tokenservice"

	"github.com/gin-gonic/gin"
)

type AuthController struct {
	LoginUsecase    domain.LoginUsecase
	RegisterUsecase domain.RegisterUsecase
	LoginLogUsecase domain.LoginLogUseCase
	RoleRepository  domain.RoleRepository
	Env             *bootstrap.Env
	SecurityManager *internal.LoginSecurityManager
	TokenService    *tokenservice.TokenService
	CasManager      casbin.Manager
}

// getClientIP 获取客户端真实IP地址
func getClientIP(c *gin.Context) string {
	// 检查 X-Forwarded-For header
	xForwardedFor := c.Request.Header.Get("X-Forwarded-For")
	if xForwardedFor != "" {
		// X-Forwarded-For 可能包含多个IP，取第一个
		ips := strings.Split(xForwardedFor, ",")
		if len(ips) > 0 {
			ip := strings.TrimSpace(ips[0])
			if net.ParseIP(ip) != nil {
				return ip
			}
		}
	}

	// 检查 X-Real-IP header
	xRealIP := c.Request.Header.Get("X-Real-IP")
	if xRealIP != "" {
		if net.ParseIP(xRealIP) != nil {
			return xRealIP
		}
	}

	// 如果没有代理头，使用 RemoteAddr
	ip, _, err := net.SplitHostPort(c.Request.RemoteAddr)
	if err != nil {
		return c.Request.RemoteAddr
	}
	return ip
}

// Login godoc
// @Summary      User login
// @Description  Authenticate user and return JWT tokens with brute force protection
// @Tags         Authentication
// @Accept       json
// @Produce      json
// @Param        request  body      domain.LoginRequest  true  "Login credentials"
// @Success      200  {object}  domain.Response{data=domain.LoginResponse}  "Login successful"
// @Failure      400  {object}  domain.Response  "Invalid request format"
// @Failure      401  {object}  domain.Response  "Invalid credentials"
// @Failure      423  {object}  domain.Response  "Account temporarily locked due to too many failed attempts"
// @Failure      500  {object}  domain.Response  "Internal server error"
// @Router       /login [post]
func (lc *AuthController) Login(c *gin.Context) {
	var request domain.LoginRequest

	err := c.ShouldBind(&request)
	if err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError("Invalid request format"))
		return
	}

	// 获取客户端IP和User-Agent
	clientIP := getClientIP(c)
	userAgent := c.Request.Header.Get("User-Agent")

	// 创建记录登录日志的辅助函数
	recordLoginLog := func(status, failureReason string) {
		if lc.LoginLogUsecase != nil {
			logRequest := &domain.CreateLoginLogRequest{
				Username:      request.UserName,
				LoginIP:       clientIP,
				UserAgent:     userAgent,
				Status:        status,
				FailureReason: failureReason,
			}

			// 异步记录日志，不阻塞登录流程
			go func() {
				_, logErr := lc.LoginLogUsecase.CreateLoginLog(c, logRequest)
				if logErr != nil {
					fmt.Printf("Failed to record login log: %v\n", logErr)
				}
			}()
		}
	}

	// 检查SecurityManager是否初始化
	if lc.SecurityManager == nil {
		fmt.Println("SecurityManager is nil!")
		c.JSON(http.StatusInternalServerError, domain.RespError("Security manager not initialized"))
		return
	}

	// 检查账号是否被锁定
	fmt.Printf("Checking if user %s is locked...\n", request.UserName)
	if lc.SecurityManager.IsLocked(request.UserName) {
		remainingTime := lc.SecurityManager.GetRemainingLockTime(request.UserName)
		lockMessage := fmt.Sprintf("账户已被锁定，请在 %d 秒后重试", int(remainingTime.Seconds()))
		fmt.Printf("User %s is locked for %d seconds\n", request.UserName, int(remainingTime.Seconds()))
		c.JSON(http.StatusLocked, domain.RespError(lockMessage))
		return
	}

	user, err := lc.LoginUsecase.GetUserByUserName(c, request.UserName)
	if err != nil {
		// 记录失败尝试（用户不存在也算失败尝试）
		fmt.Printf("User %s not found, recording failed attempt\n", request.UserName)
		lc.SecurityManager.RecordFailedAttempt(request.UserName)

		// 记录登录失败日志
		recordLoginLog("failed", "用户不存在")

		c.JSON(http.StatusUnauthorized, domain.RespError("用户名或密码错误"))
		return
	}

	// 验证密码
	err = bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(request.Password))
	if err != nil {
		// 记录失败尝试
		fmt.Printf("Password incorrect for user %s, recording failed attempt\n", request.UserName)
		lc.SecurityManager.RecordFailedAttempt(request.UserName)

		// 记录登录失败日志
		recordLoginLog("failed", "密码错误")

		// 检查是否刚被锁定
		fmt.Printf("Checking if user %s is now locked after failed attempt\n", request.UserName)
		if lc.SecurityManager.IsLocked(request.UserName) {
			remainingTime := lc.SecurityManager.GetRemainingLockTime(request.UserName)
			lockMessage := fmt.Sprintf("密码错误次数过多，账户已被锁定 %d 秒", int(remainingTime.Seconds()))
			fmt.Printf("User %s is now locked for %d seconds\n", request.UserName, int(remainingTime.Seconds()))
			c.JSON(http.StatusLocked, domain.RespError(lockMessage))
			return
		}

		// 显示剩余尝试次数
		failedAttempts := lc.SecurityManager.GetFailedAttempts(request.UserName)
		remainingAttempts := lc.SecurityManager.MaxFailures - failedAttempts
		fmt.Printf("User %s has %d failed attempts, %d remaining\n", request.UserName, failedAttempts, remainingAttempts)
		if remainingAttempts > 0 {
			errorMessage := fmt.Sprintf("用户名或密码错误，还可尝试 %d 次", remainingAttempts)
			c.JSON(http.StatusUnauthorized, domain.RespError(errorMessage))
		} else {
			c.JSON(http.StatusUnauthorized, domain.RespError("用户名或密码错误"))
		}
		return
	}

	// 登录成功，清除失败记录
	lc.SecurityManager.RecordSuccessfulLogin(request.UserName)

	accessToken, err := lc.TokenService.CreateAccessToken(user, lc.Env.AccessTokenSecret, lc.Env.AccessTokenExpiryHour)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	refreshToken, err := lc.TokenService.CreateRefreshToken(user, lc.Env.RefreshTokenSecret, lc.Env.RefreshTokenExpiryHour)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	loginResponse := domain.LoginResponse{
		AccessToken:  accessToken,
		RefreshToken: refreshToken,
	}

	// 记录登录成功日志
	recordLoginLog("success", "")

	c.JSON(http.StatusOK, domain.RespSuccess(loginResponse))
}

// RefreshToken godoc
// @Summary      Refresh access token
// @Description  Refresh access token using refresh token
// @Tags         Authentication
// @Accept       json
// @Produce      json
// @Param        request  body      domain.RefreshTokenRequest  true  "Refresh token request"
// @Success      200  {object}  domain.Response{data=domain.RefreshTokenResponse}  "Token refreshed successfully"
// @Failure      400  {object}  domain.Response  "Invalid request format"
// @Failure      401  {object}  domain.Response  "Invalid refresh token"
// @Failure      500  {object}  domain.Response  "Internal server error"
// @Router       /auth/refresh [post]
func (lc *AuthController) RefreshToken(c *gin.Context) {
	var request domain.RefreshTokenRequest

	err := c.ShouldBind(&request)
	if err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError("Invalid request format"))
		return
	}

	// 验证刷新令牌是否有效
	isValid, err := lc.TokenService.IsAuthorized(request.RefreshToken, lc.Env.RefreshTokenSecret)
	if err != nil || !isValid {
		c.JSON(http.StatusUnauthorized, domain.RespError("Invalid refresh token"))
		return
	}

	// 从刷新令牌中提取用户ID
	userID, err := lc.TokenService.ExtractIDFromToken(request.RefreshToken, lc.Env.RefreshTokenSecret)
	if err != nil {
		c.JSON(http.StatusUnauthorized, domain.RespError("Invalid refresh token"))
		return
	}

	// 根据用户ID获取用户信息
	user, err := lc.LoginUsecase.GetUserByID(c, userID)
	if err != nil {
		c.JSON(http.StatusUnauthorized, domain.RespError("User not found"))
		return
	}

	// 创建新的访问令牌
	newAccessToken, err := lc.TokenService.CreateAccessToken(user, lc.Env.AccessTokenSecret, lc.Env.AccessTokenExpiryHour)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError("Failed to create access token"))
		return
	}

	// 创建新的刷新令牌
	newRefreshToken, err := lc.TokenService.CreateRefreshToken(user, lc.Env.RefreshTokenSecret, lc.Env.RefreshTokenExpiryHour)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError("Failed to create refresh token"))
		return
	}

	refreshResponse := domain.RefreshTokenResponse{
		AccessToken:  newAccessToken,
		RefreshToken: newRefreshToken,
	}

	c.JSON(http.StatusOK, domain.RespSuccess(refreshResponse))
}

// Logout godoc
// @Summary      User logout
// @Description  Logout user and invalidate tokens
// @Tags         Authentication
// @Accept       json
// @Produce      json
// @Param        request  body      domain.LogoutRequest  false  "Logout request (optional)"
// @Success      200  {object}  domain.Response  "Logout successful"
// @Failure      400  {object}  domain.Response  "Invalid request format"
// @Failure      401  {object}  domain.Response  "Not authorized"
// @Router       /auth/logout [post]
func (lc *AuthController) Logout(c *gin.Context) {
	var request domain.LogoutRequest

	// 解析请求体（可选）
	err := c.ShouldBind(&request)
	if err != nil && err.Error() != "EOF" {
		c.JSON(http.StatusBadRequest, domain.RespError("Invalid request format"))
		return
	}

	// 从请求头中提取访问令牌
	authHeader := c.Request.Header.Get("Authorization")
	var accessToken string
	if authHeader != "" {
		parts := strings.Split(authHeader, " ")
		if len(parts) == 2 && parts[0] == "Bearer" {
			accessToken = parts[1]
		}
	}

	// 提取用户信息用于日志记录
	var userID, userName string
	if accessToken != "" {
		// 从令牌中提取用户信息（即使令牌即将失效，我们仍然可以从中提取信息用于日志）
		if claims, err := lc.TokenService.ExtractAllClaimsFromToken(accessToken, lc.Env.AccessTokenSecret); err == nil {
			userID = claims.ID
			userName = claims.Name
		}
	}

	// 记录登出日志
	fmt.Printf("User logout - UserID: %s, UserName: %s, AccessToken: %s...\n",
		userID, userName,
		func() string {
			if len(accessToken) > 10 {
				return accessToken[:10]
			}
			return accessToken
		}())

	// 在更完整的实现中，可以：
	// 1. 将访问令牌和刷新令牌加入黑名单（Redis/内存缓存）
	// 2. 清除服务器端的会话信息
	// 3. 通知其他服务用户已登出
	//
	// 示例实现（需要添加相应的基础设施）：
	// if accessToken != "" {
	//     lc.TokenBlacklist.Add(accessToken, time.Until(tokenExpiry))
	// }
	// if request.RefreshToken != "" {
	//     lc.TokenBlacklist.Add(request.RefreshToken, time.Until(refreshTokenExpiry))
	// }

	c.JSON(http.StatusOK, domain.RespSuccess("Logout successful"))
}

// ClientLogin handles login requests from heji Android client
// Client sends {"tel": "username", "password": "hash"} and expects {"code":0, "data": "jwt_token"}
func (lc *AuthController) ClientLogin(c *gin.Context) {
	var request struct {
		Tel      string `json:"tel" binding:"required"`
		Password string `json:"password" binding:"required"`
	}

	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusOK, domain.RespError("Invalid request format"))
		return
	}

	user, err := lc.LoginUsecase.GetUserByPhone(c, request.Tel)
	if err != nil {
		c.JSON(http.StatusOK, domain.RespError("用户名或密码错误"))
		return
	}

	if err = bcrypt.CompareHashAndPassword([]byte(user.Password), []byte(request.Password)); err != nil {
		c.JSON(http.StatusOK, domain.RespError("用户名或密码错误"))
		return
	}

	accessToken, err := lc.TokenService.CreateAccessToken(user, lc.Env.AccessTokenSecret, lc.Env.AccessTokenExpiryHour)
	if err != nil {
		c.JSON(http.StatusOK, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess(accessToken))
}

// Register godoc
// @Summary      User registration
// @Description  Register a new user account
// @Tags         Authentication
// @Accept       json
// @Produce      json
// @Param        request  body      domain.RegisterRequest  true  "Registration info"
// @Success      200  {object}  domain.Response  "Registration successful"
// @Failure      400  {object}  domain.Response  "Invalid request format"
// @Failure      409  {object}  domain.Response  "User already exists"
// @Failure      500  {object}  domain.Response  "Internal server error"
// @Router       /Register [post]
func (lc *AuthController) Register(c *gin.Context) {
	var request domain.RegisterRequest

	if err := c.ShouldBind(&request); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError("Invalid request format"))
		return
	}

	// 检查用户名是否已存在
	_, err := lc.RegisterUsecase.GetUserByUserName(c, request.Tel)
	if err == nil {
		c.JSON(http.StatusConflict, domain.RespError("用户已存在"))
		return
	}

	user := &domain.User{
		Username: request.Tel,
		Phone:    request.Tel,
		Password: request.Password,
		Status:   "active",
		IsActive: true,
	}

	if request.Name != "" {
		user.Username = request.Name
	}

	if err := lc.RegisterUsecase.CreateUser(c, user); err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	// 自动分配 "user" 角色
	if lc.RoleRepository != nil && lc.CasManager != nil {
		if userRole, err := lc.RoleRepository.GetByName(c, "user"); err == nil {
			if err := lc.RoleRepository.Assign(c, user.ID, userRole.ID); err != nil {
				fmt.Printf("assign user role failed: %v\n", err)
			}
			if _, err := lc.CasManager.AddRoleForUser(user.ID, userRole.ID); err != nil {
				fmt.Printf("casbin add role for user failed: %v\n", err)
			}
		}
	}

	c.JSON(http.StatusOK, domain.RespSuccess("注册成功"))
}
