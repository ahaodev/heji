package usecase

import (
	"context"
	"shadmin/domain"
	"strings"
	"time"
)

type loginLogUsecase struct {
	loginLogRepository domain.LoginLogRepository
	contextTimeout     time.Duration
}

func NewLoginLogUsecase(loginLogRepository domain.LoginLogRepository, timeout time.Duration) domain.LoginLogUseCase {
	return &loginLogUsecase{
		loginLogRepository: loginLogRepository,
		contextTimeout:     timeout,
	}
}

// parseUserAgent 解析User-Agent字符串提取浏览器和操作系统信息
func (luc *loginLogUsecase) parseUserAgent(userAgent string) (browser, os, device string) {
	ua := strings.ToLower(userAgent)

	// 解析浏览器
	switch {
	case strings.Contains(ua, "firefox"):
		browser = "Firefox"
	case strings.Contains(ua, "chrome") && !strings.Contains(ua, "edg"):
		browser = "Chrome"
	case strings.Contains(ua, "edg"):
		browser = "Edge"
	case strings.Contains(ua, "safari") && !strings.Contains(ua, "chrome"):
		browser = "Safari"
	case strings.Contains(ua, "opera"):
		browser = "Opera"
	case strings.Contains(ua, "ie") || strings.Contains(ua, "trident"):
		browser = "Internet Explorer"
	default:
		browser = "Unknown"
	}

	// 解析操作系统
	switch {
	case strings.Contains(ua, "windows nt 10.0"):
		os = "Windows 10"
	case strings.Contains(ua, "windows nt 6.3"):
		os = "Windows 8.1"
	case strings.Contains(ua, "windows nt 6.1"):
		os = "Windows 7"
	case strings.Contains(ua, "windows"):
		os = "Windows"
	case strings.Contains(ua, "macintosh") || strings.Contains(ua, "mac os x"):
		os = "macOS"
	case strings.Contains(ua, "linux"):
		os = "Linux"
	case strings.Contains(ua, "android"):
		os = "Android"
	case strings.Contains(ua, "iphone") || strings.Contains(ua, "ipad"):
		os = "iOS"
	default:
		os = "Unknown"
	}

	// 解析设备类型
	switch {
	case strings.Contains(ua, "mobile"):
		device = "Mobile"
	case strings.Contains(ua, "tablet") || strings.Contains(ua, "ipad"):
		device = "Tablet"
	default:
		device = "Desktop"
	}

	return browser, os, device
}

func (luc *loginLogUsecase) CreateLoginLog(c context.Context, request *domain.CreateLoginLogRequest) (*domain.LoginLog, error) {
	ctx, cancel := context.WithTimeout(c, luc.contextTimeout)
	defer cancel()

	// 解析User-Agent字符串
	browser, os, device := luc.parseUserAgent(request.UserAgent)

	// 如果请求中没有提供browser、os、device，则使用解析的结果
	if request.Browser == "" {
		request.Browser = browser
	}
	if request.OS == "" {
		request.OS = os
	}
	if request.Device == "" {
		request.Device = device
	}

	loginLog := &domain.LoginLog{
		Username:      request.Username,
		LoginIP:       request.LoginIP,
		UserAgent:     request.UserAgent,
		Browser:       request.Browser,
		OS:            request.OS,
		Device:        request.Device,
		Status:        request.Status,
		FailureReason: request.FailureReason,
		LoginTime:     time.Now(),
	}

	if err := luc.loginLogRepository.Create(ctx, loginLog); err != nil {
		return nil, err
	}

	return loginLog, nil
}

func (luc *loginLogUsecase) ListLoginLogs(c context.Context, filter domain.LoginLogQueryFilter) (*domain.PagedResult[*domain.LoginLog], error) {
	ctx, cancel := context.WithTimeout(c, luc.contextTimeout)
	defer cancel()
	return luc.loginLogRepository.Query(ctx, filter)
}

func (luc *loginLogUsecase) ClearAllLoginLogs(c context.Context) error {
	ctx, cancel := context.WithTimeout(c, luc.contextTimeout)
	defer cancel()
	return luc.loginLogRepository.ClearAll(ctx)
}
