package repository

import (
	"context"
	"shadmin/domain"
	"shadmin/ent"
	"shadmin/ent/loginlog"
)

// Helper function to convert domain status string to ent status enum
func domainStatusToEntLoginLogStatus(status string) loginlog.Status {
	switch status {
	case "success":
		return loginlog.StatusSuccess
	case "failed":
		return loginlog.StatusFailed
	default:
		return loginlog.StatusFailed
	}
}

// Helper function to convert ent status enum to domain status string
func entLoginLogStatusToDomainStatus(status loginlog.Status) string {
	return string(status)
}

type entLoginLogRepository struct {
	client *ent.Client
}

func NewLoginLogRepository(client *ent.Client) domain.LoginLogRepository {
	return &entLoginLogRepository{
		client: client,
	}
}

func (lr *entLoginLogRepository) Create(c context.Context, log *domain.LoginLog) error {
	status := domainStatusToEntLoginLogStatus(log.Status)

	created, err := lr.client.LoginLog.
		Create().
		SetUsername(log.Username).
		SetLoginIP(log.LoginIP).
		SetUserAgent(log.UserAgent).
		SetNillableBrowser(&log.Browser).
		SetNillableOs(&log.OS).
		SetNillableDevice(&log.Device).
		SetStatus(status).
		SetNillableFailureReason(&log.FailureReason).
		SetLoginTime(log.LoginTime).
		Save(c)

	if err != nil {
		return err
	}

	log.ID = created.ID
	log.Status = entLoginLogStatusToDomainStatus(created.Status)
	log.LoginTime = created.LoginTime
	return nil
}

func (lr *entLoginLogRepository) Query(c context.Context, filter domain.LoginLogQueryFilter) (*domain.LoginLogPagedResult, error) {
	// 构建基础查询
	baseQuery := lr.client.LoginLog.Query()

	if filter.Username != "" {
		baseQuery = baseQuery.Where(loginlog.UsernameContains(filter.Username))
	}
	if filter.LoginIP != "" {
		baseQuery = baseQuery.Where(loginlog.LoginIPContains(filter.LoginIP))
	}
	if filter.Status != "" {
		baseQuery = baseQuery.Where(loginlog.StatusEQ(domainStatusToEntLoginLogStatus(filter.Status)))
	}
	if filter.Browser != "" {
		baseQuery = baseQuery.Where(loginlog.BrowserContains(filter.Browser))
	}
	if filter.OS != "" {
		baseQuery = baseQuery.Where(loginlog.OsContains(filter.OS))
	}
	if filter.StartTime != nil {
		baseQuery = baseQuery.Where(loginlog.LoginTimeGTE(*filter.StartTime))
	}
	if filter.EndTime != nil {
		baseQuery = baseQuery.Where(loginlog.LoginTimeLTE(*filter.EndTime))
	}

	// 获取总数
	total, err := baseQuery.Clone().Count(c)
	if err != nil {
		return nil, err
	}

	// 应用排序（默认按登录时间倒序）
	if filter.SortBy != "" {
		switch filter.SortBy {
		case "username":
			if filter.Order == "desc" {
				baseQuery = baseQuery.Order(ent.Desc(loginlog.FieldUsername))
			} else {
				baseQuery = baseQuery.Order(ent.Asc(loginlog.FieldUsername))
			}
		case "login_ip":
			if filter.Order == "desc" {
				baseQuery = baseQuery.Order(ent.Desc(loginlog.FieldLoginIP))
			} else {
				baseQuery = baseQuery.Order(ent.Asc(loginlog.FieldLoginIP))
			}
		case "status":
			if filter.Order == "desc" {
				baseQuery = baseQuery.Order(ent.Desc(loginlog.FieldStatus))
			} else {
				baseQuery = baseQuery.Order(ent.Asc(loginlog.FieldStatus))
			}
		default:
			if filter.Order == "desc" {
				baseQuery = baseQuery.Order(ent.Desc(loginlog.FieldLoginTime))
			} else {
				baseQuery = baseQuery.Order(ent.Asc(loginlog.FieldLoginTime))
			}
		}
	} else {
		// 默认按登录时间倒序
		baseQuery = baseQuery.Order(ent.Desc(loginlog.FieldLoginTime))
	}

	// 应用分页
	var logs []*ent.LoginLog
	if filter.Page > 0 && filter.PageSize > 0 {
		offset := (filter.Page - 1) * filter.PageSize
		logs, err = baseQuery.Offset(offset).Limit(filter.PageSize).All(c)
	} else {
		logs, err = baseQuery.All(c)
	}
	if err != nil {
		return nil, err
	}

	// 转换为 domain.LoginLog
	var result []*domain.LoginLog
	for _, log := range logs {
		domainLog := &domain.LoginLog{
			ID:            log.ID,
			Username:      log.Username,
			LoginIP:       log.LoginIP,
			UserAgent:     log.UserAgent,
			Browser:       log.Browser,
			OS:            log.Os,
			Device:        log.Device,
			Status:        entLoginLogStatusToDomainStatus(log.Status),
			FailureReason: log.FailureReason,
			LoginTime:     log.LoginTime,
		}
		result = append(result, domainLog)
	}

	return domain.NewPagedResult(result, total, filter.Page, filter.PageSize), nil
}

func (lr *entLoginLogRepository) ClearAll(c context.Context) error {
	_, err := lr.client.LoginLog.
		Delete().
		Exec(c)
	return err
}
