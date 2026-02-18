package repository

import (
	"context"
	"shadmin/domain"
	"shadmin/ent"
	"shadmin/ent/user"
	"shadmin/internal/casbin"
)

// Helper function to convert domain status string to ent status enum
func domainStatusToEntStatus(status string) user.Status {
	switch status {
	case "active":
		return user.StatusActive
	case "inactive":
		return user.StatusInactive
	case "invited":
		return user.StatusInvited
	case "suspended":
		return user.StatusSuspended
	default:
		return user.StatusActive
	}
}

// Helper function to convert ent status enum to domain status string
func entStatusToDomainStatus(status user.Status) string {
	return string(status)
}

// entUserToDomainUser converts an ent.User to domain.User and extracts role IDs from edges
func entUserToDomainUser(u *ent.User) *domain.User {
	domainUser := &domain.User{
		ID:        u.ID,
		Username:  u.Username,
		Email:     u.Email,
		Phone:     u.Phone,
		Password:  u.Password,
		Avatar:    u.Avatar,
		IsAdmin:   u.IsAdmin,
		Status:    entStatusToDomainStatus(u.Status),
		CreatedAt: u.CreatedAt,
		UpdatedAt: u.UpdatedAt,
	}

	// Extract role IDs from database relationship
	if u.Edges.Roles != nil {
		var roleIDs []string
		for _, role := range u.Edges.Roles {
			roleIDs = append(roleIDs, role.ID)
		}
		domainUser.Roles = roleIDs
		if len(roleIDs) > 0 {
			domainUser.IsActive = true
		}
	}

	return domainUser
}

type entUserRepository struct {
	client     *ent.Client
	casManager casbin.Manager
}

func NewUserRepository(client *ent.Client, casManager casbin.Manager) domain.UserRepository {
	return &entUserRepository{
		client:     client,
		casManager: casManager,
	}
}

func (ur *entUserRepository) Create(c context.Context, u *domain.User) error {
	status := domainStatusToEntStatus(u.Status)

	created, err := ur.client.User.
		Create().
		SetUsername(u.Username).
		SetEmail(u.Email).
		SetPhone(u.Phone).
		SetPassword(u.Password).
		SetAvatar(u.Avatar).
		SetStatus(status).
		SetNillableInvitedAt(u.InvitedAt).
		SetNillableInvitedBy(&u.InvitedBy).
		Save(c)

	if err != nil {
		return err
	}

	u.ID = created.ID
	u.Status = entStatusToDomainStatus(created.Status)
	u.CreatedAt = created.CreatedAt
	u.UpdatedAt = created.UpdatedAt
	return nil
}

func (ur *entUserRepository) Query(c context.Context, filter domain.UserQueryFilter) (*domain.UserPagedResult, error) {
	// 构建基础查询
	baseQuery := ur.client.User.Query()

	if filter.Status != "" {
		baseQuery = baseQuery.Where(user.StatusEQ(domainStatusToEntStatus(filter.Status)))
	}
	if filter.Username != "" {
		baseQuery = baseQuery.Where(user.UsernameContains(filter.Username))
	}
	if filter.Email != "" {
		baseQuery = baseQuery.Where(user.EmailContains(filter.Email))
	}
	if filter.IsAdmin != nil {
		baseQuery = baseQuery.Where(user.IsAdmin(*filter.IsAdmin))
	}

	// 默认排除 admin 用户（除非明确查询）
	//if filter.Username != "admin" {
	//	baseQuery = baseQuery.Where(user.Not(user.Username("admin")))
	//}

	// 获取总数
	total, err := baseQuery.Clone().Count(c)
	if err != nil {
		return nil, err
	}

	// 应用排序（在Select之前）
	if filter.SortBy != "" {
		switch filter.SortBy {
		case "username":
			if filter.Order == "desc" {
				baseQuery = baseQuery.Order(ent.Desc(user.FieldUsername))
			} else {
				baseQuery = baseQuery.Order(ent.Asc(user.FieldUsername))
			}
		case "email":
			if filter.Order == "desc" {
				baseQuery = baseQuery.Order(ent.Desc(user.FieldEmail))
			} else {
				baseQuery = baseQuery.Order(ent.Asc(user.FieldEmail))
			}
		default:
			if filter.Order == "desc" {
				baseQuery = baseQuery.Order(ent.Desc(user.FieldCreatedAt))
			} else {
				baseQuery = baseQuery.Order(ent.Asc(user.FieldCreatedAt))
			}
		}
	}

	// 构建数据查询 (移除租户信息预加载)
	query := baseQuery

	// 应用分页
	var users []*ent.User
	if filter.Page > 0 && filter.PageSize > 0 {
		offset := (filter.Page - 1) * filter.PageSize
		users, err = query.Offset(offset).Limit(filter.PageSize).All(c)
	} else {
		users, err = query.All(c)
	}
	if err != nil {
		return nil, err
	}

	// 转换为 domain.User (移除租户相关代码)
	var result []*domain.User
	for _, u := range users {
		domainUser := &domain.User{
			ID:        u.ID,
			Username:  u.Username,
			Email:     u.Email,
			Phone:     u.Phone,
			Avatar:    u.Avatar,
			IsAdmin:   u.IsAdmin,
			Status:    entStatusToDomainStatus(u.Status),
			CreatedAt: u.CreatedAt,
			UpdatedAt: u.UpdatedAt,
		}

		// 如果需要包含角色信息 (从数据库关系获取)
		if filter.IncludeRoles {
			// 重新查询包含角色信息的用户
			userWithRoles, err := ur.client.User.
				Query().
				Where(user.ID(u.ID)).
				WithRoles().
				First(c)
			if err == nil && userWithRoles.Edges.Roles != nil {
				var roleIDs []string
				for _, role := range userWithRoles.Edges.Roles {
					roleIDs = append(roleIDs, role.ID)
				}
				domainUser.Roles = roleIDs
				if len(roleIDs) > 0 {
					domainUser.IsActive = true // 有角色说明是活跃的
				}
			}
		}

		result = append(result, domainUser)
	}

	return domain.NewPagedResult(result, total, filter.Page, filter.PageSize), nil
}

func (ur *entUserRepository) GetByUsername(c context.Context, userName string) (*domain.User, error) {
	u, err := ur.client.User.
		Query().
		Where(user.Username(userName)).
		WithRoles().
		First(c)

	if err != nil {
		return nil, err
	}

	return entUserToDomainUser(u), nil
}

func (ur *entUserRepository) GetByEmail(c context.Context, email string) (*domain.User, error) {
	u, err := ur.client.User.
		Query().
		Where(user.Email(email)).
		WithRoles().
		First(c)

	if err != nil {
		return nil, err
	}

	return entUserToDomainUser(u), nil
}

func (ur *entUserRepository) GetByPhone(c context.Context, phone string) (*domain.User, error) {
	u, err := ur.client.User.
		Query().
		Where(user.Phone(phone)).
		WithRoles().
		First(c)

	if err != nil {
		return nil, err
	}

	return entUserToDomainUser(u), nil
}

func (ur *entUserRepository) GetByID(c context.Context, id string) (*domain.User, error) {
	u, err := ur.client.User.
		Query().
		Where(user.ID(id)).
		WithRoles().
		First(c)

	if err != nil {
		return nil, err
	}

	return entUserToDomainUser(u), nil
}

func (ur *entUserRepository) Update(c context.Context, u *domain.User) error {
	updateQuery := ur.client.User.
		UpdateOneID(u.ID).
		SetUsername(u.Username).
		SetEmail(u.Email).
		SetPhone(u.Phone).
		SetAvatar(u.Avatar).
		SetStatus(domainStatusToEntStatus(u.Status))

	// 🔒 如果提供了密码，则更新密码哈希
	if u.Password != "" {
		updateQuery = updateQuery.SetPassword(u.Password)
	}

	updated, err := updateQuery.Save(c)

	if err != nil {
		return err
	}

	u.UpdatedAt = updated.UpdatedAt
	return nil
}

func (ur *entUserRepository) Delete(c context.Context, id string) error {
	// 1. 获取用户当前的角色，以便清理 casbin 规则
	u, err := ur.client.User.
		Query().
		Where(user.ID(id)).
		WithRoles().
		First(c)
	if err != nil {
		return err
	}

	// 2. 清理 casbin 中的用户-角色映射 (g 类型规则)
	if u.Edges.Roles != nil {
		for _, role := range u.Edges.Roles {
			// 删除 casbin 中 "g, userID, roleID" 的记录
			_, _ = ur.casManager.DeleteRoleForUser(id, role.ID)
		}
	}

	// 3. 删除用户记录 (ent 会自动清理 user_roles 中间表)
	return ur.client.User.
		DeleteOneID(id).
		Exec(c)
}
