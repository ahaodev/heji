package domain

import "context"

// SyncChangesResponse 增量拉取响应
type SyncChangesResponse struct {
	Books     []*Book `json:"books"`
	Bills     []*Bill `json:"bills"`
	HasMore   bool    `json:"has_more"`
	NextSince int64   `json:"next_since"`
}

// SyncRepository 同步数据查询接口
type SyncRepository interface {
	// QueryChanges 查询 since 时间戳之后的所有变更（含已删除），限制数量
	QueryChanges(c context.Context, userID string, sinceMs int64, limit int) (*SyncChangesResponse, error)
}
