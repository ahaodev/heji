package repository

import (
	"context"
	"shadmin/domain"
	"shadmin/ent"
	"shadmin/ent/loginlog"
	"shadmin/ent/predicate"
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
	// 构建查询条件
	var predicates []predicate.LoginLog
	if filter.Username != "" {
		predicates = append(predicates, loginlog.UsernameContains(filter.Username))
	}
	if filter.LoginIP != "" {
		predicates = append(predicates, loginlog.LoginIPContains(filter.LoginIP))
	}
	if filter.Status != "" {
		predicates = append(predicates, loginlog.StatusEQ(domainStatusToEntLoginLogStatus(filter.Status)))
	}
	if filter.Browser != "" {
		predicates = append(predicates, loginlog.BrowserContains(filter.Browser))
	}
	if filter.OS != "" {
		predicates = append(predicates, loginlog.OsContains(filter.OS))
	}
	if filter.StartTime != nil {
		predicates = append(predicates, loginlog.LoginTimeGTE(*filter.StartTime))
	}
	if filter.EndTime != nil {
		predicates = append(predicates, loginlog.LoginTimeLTE(*filter.EndTime))
	}
	baseQuery := lr.client.LoginLog.Query().Where(predicates...)

	// 获取总数
	total, err := baseQuery.Clone().Count(c)
	if err != nil {
		return nil, err
	}

	// 应用排序（默认按登录时间倒序）
	if filter.SortBy != "" {
		baseQuery = baseQuery.Order(ApplySorting(filter.SortBy, filter.Order, map[string]string{
			"username": loginlog.FieldUsername,
			"login_ip": loginlog.FieldLoginIP,
			"status":   loginlog.FieldStatus,
		}, loginlog.FieldLoginTime))
	} else {
		// 默认按登录时间倒序
		baseQuery = baseQuery.Order(ent.Desc(loginlog.FieldLoginTime))
	}

	// 应用分页
	if offset, limit, ok := filter.Paginate(); ok {
		baseQuery = baseQuery.Offset(offset).Limit(limit)
	}
	logs, err := baseQuery.All(c)
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
