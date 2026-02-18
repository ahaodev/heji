package domain

import (
	"context"
	"time"
)

// LoginLog 结构体定义了登录日志的信息
type LoginLog struct {
	ID            string    `json:"id"`
	Username      string    `json:"username"`
	LoginIP       string    `json:"login_ip"`
	UserAgent     string    `json:"user_agent"`
	Browser       string    `json:"browser,omitempty"`
	OS            string    `json:"os,omitempty"`
	Device        string    `json:"device,omitempty"`
	Status        string    `json:"status"` // success, failed
	FailureReason string    `json:"failure_reason,omitempty"`
	LoginTime     time.Time `json:"login_time"`
}

// CreateLoginLogRequest 创建登录日志请求
type CreateLoginLogRequest struct {
	Username      string `json:"username" binding:"required"`
	LoginIP       string `json:"login_ip" binding:"required"`
	UserAgent     string `json:"user_agent" binding:"required"`
	Browser       string `json:"browser,omitempty"`
	OS            string `json:"os,omitempty"`
	Device        string `json:"device,omitempty"`
	Status        string `json:"status" binding:"required"` // success, failed
	FailureReason string `json:"failure_reason,omitempty"`
}

// LoginLogPagedResult 登录日志分页查询结果
type LoginLogPagedResult = PagedResult[*LoginLog]

// LoginLogQueryFilter 登录日志查询过滤器
type LoginLogQueryFilter struct {
	Username  string     `json:"username,omitempty"`
	LoginIP   string     `json:"login_ip,omitempty"`
	Status    string     `json:"status,omitempty"`
	Browser   string     `json:"browser,omitempty"`
	OS        string     `json:"os,omitempty"`
	StartTime *time.Time `json:"start_time,omitempty"`
	EndTime   *time.Time `json:"end_time,omitempty"`
	QueryParams
}

type LoginLogRepository interface {
	Create(c context.Context, loginLog *LoginLog) error
	Query(c context.Context, filter LoginLogQueryFilter) (*PagedResult[*LoginLog], error)
	ClearAll(c context.Context) error
}

type LoginLogUseCase interface {
	CreateLoginLog(c context.Context, request *CreateLoginLogRequest) (*LoginLog, error)
	ListLoginLogs(c context.Context, filter LoginLogQueryFilter) (*PagedResult[*LoginLog], error)
	ClearAllLoginLogs(c context.Context) error
}
