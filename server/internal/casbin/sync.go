package casbin

import (
	"context"
	"fmt"
	"shadmin/ent"
	"shadmin/ent/apiresource"
	"shadmin/ent/role"
	"shadmin/ent/user"
	"time"
)

// SyncService casbin同步服务
type SyncService struct {
	entClient *ent.Client
	manager   Manager
	logger    Logger
}

// NewSyncService 创建新的同步服务实例
func NewSyncService(entClient *ent.Client, manager Manager) *SyncService {
	return &SyncService{
		entClient: entClient,
		manager:   manager,
		logger:    &defaultLogger{},
	}
}

// NewSyncServiceWithLogger 创建带自定义日志的同步服务实例
func NewSyncServiceWithLogger(entClient *ent.Client, manager Manager, logger Logger) *SyncService {
	return &SyncService{
		entClient: entClient,
		manager:   manager,
		logger:    logger,
	}
}

// SyncFromDatabase 从数据库同步所有casbin数据
// 这是主要的同步方法，会清空现有的casbin数据并重新从数据库加载
func (s *SyncService) SyncFromDatabase(ctx context.Context) error {
	s.logger.Log("SyncFromDatabase", "开始从数据库同步casbin数据")

	startTime := time.Now()

	// 1. 清空现有的casbin策略
	if err := s.clearCasbinPolicies(ctx); err != nil {
		return fmt.Errorf("清空casbin策略失败: %w", err)
	}

	// 2. 同步用户角色关系
	if err := s.syncUserRoles(ctx); err != nil {
		return fmt.Errorf("同步用户角色关系失败: %w", err)
	}

	// 3. 同步角色权限策略
	if err := s.syncRolePermissions(ctx); err != nil {
		return fmt.Errorf("同步角色权限策略失败: %w", err)
	}

	// 4. 保存策略到数据库
	if err := s.manager.SavePolicy(); err != nil {
		return fmt.Errorf("保存casbin策略失败: %w", err)
	}

	duration := time.Since(startTime)
	s.logger.Log("SyncFromDatabase", fmt.Sprintf("同步完成，耗时: %v", duration))

	return nil
}

// clearCasbinPolicies 清空所有casbin策略
func (s *SyncService) clearCasbinPolicies(ctx context.Context) error {
	s.logger.Log("clearCasbinPolicies", "清空现有casbin策略")

	// 清空所有权限策略 (p规则)
	allPolicies := s.manager.GetAllPolicies()
	for _, policy := range allPolicies {
		if len(policy) >= 3 {
			if _, err := s.manager.RemovePolicy(policy[0], policy[1], policy[2]); err != nil {
				s.logger.Log("clearCasbinPolicies", fmt.Sprintf("移除策略失败: %v", err))
			}
		}
	}

	// 清空所有角色映射 (g规则)
	allRoles := s.manager.GetAllRoles()
	for _, roleMapping := range allRoles {
		if len(roleMapping) >= 2 {
			if _, err := s.manager.DeleteRoleForUser(roleMapping[0], roleMapping[1]); err != nil {
				s.logger.Log("clearCasbinPolicies", fmt.Sprintf("移除角色映射失败: %v", err))
			}
		}
	}

	s.logger.Log("clearCasbinPolicies", "清空策略完成")
	return nil
}

// syncUserRoles 同步用户角色关系
func (s *SyncService) syncUserRoles(ctx context.Context) error {
	s.logger.Log("syncUserRoles", "开始同步用户角色关系")

	// 查询所有活跃用户及其角色
	users, err := s.entClient.User.Query().
		Where(user.StatusEQ("active")).
		WithRoles(func(q *ent.RoleQuery) {
			q.Where(role.StatusEQ("active"))
		}).
		All(ctx)

	if err != nil {
		return fmt.Errorf("查询用户角色关系失败: %w", err)
	}

	userRoleCount := 0

	for _, u := range users {
		for _, r := range u.Edges.Roles {
			// 添加用户角色映射到casbin
			if _, err := s.manager.AddRoleForUser(u.ID, r.ID); err != nil {
				s.logger.Log("syncUserRoles", fmt.Sprintf("添加用户角色失败: user=%s, role=%s, error=%v", u.ID, r.ID, err))
				continue
			}
			userRoleCount++
		}
	}

	s.logger.Log("syncUserRoles", fmt.Sprintf("同步用户角色关系完成，共处理 %d 条关系", userRoleCount))
	return nil
}

// syncRolePermissions 同步角色权限策略
func (s *SyncService) syncRolePermissions(ctx context.Context) error {
	s.logger.Log("syncRolePermissions", "开始同步角色权限策略")

	// 查询所有活跃角色及其菜单和API资源
	roles, err := s.entClient.Role.Query().
		Where(role.StatusEQ("active")).
		WithMenus(func(q *ent.MenuQuery) {
			q.Where().
				WithAPIResources(func(ar *ent.ApiResourceQuery) {
					ar.Where(apiresource.IsPublicEQ(false)) // 只同步需要权限验证的API
				})
		}).
		All(ctx)

	if err != nil {
		return fmt.Errorf("查询角色权限关系失败: %w", err)
	}

	policyCount := 0

	for _, r := range roles {
		// 检查是否为admin角色（通过角色名称判断）
		if r.Name == "admin" {
			// admin角色获得通配符权限
			if _, err := s.manager.AddPolicy(r.ID, "*", "*"); err != nil {
				s.logger.Log("syncRolePermissions", fmt.Sprintf("添加admin通配符权限失败: role=%s, error=%v", r.ID, err))
			} else {
				s.logger.Log("syncRolePermissions", fmt.Sprintf("已为admin角色 %s 添加通配符权限", r.ID))
				policyCount++
			}
		} else {
			// 非admin角色才需要通过菜单和API资源设置权限
			for _, menu := range r.Edges.Menus {
				// 只添加菜单关联的API资源权限，不添加菜单本身的权限
				for _, apiRes := range menu.Edges.APIResources {
					// API资源权限直接使用路径作为对象，不添加前缀
					apiObject := apiRes.Path
					if _, err := s.manager.AddPolicy(r.ID, apiObject, apiRes.Method); err != nil {
						s.logger.Log("syncRolePermissions", fmt.Sprintf("添加API权限失败: role=%s, api=%s:%s, error=%v", r.ID, apiRes.Method, apiRes.Path, err))
						continue
					}
					policyCount++
				}
			}
		}
	}

	s.logger.Log("syncRolePermissions", fmt.Sprintf("同步角色权限策略完成，共处理 %d 条策略", policyCount))
	return nil
}

// SyncUserRole 同步单个用户的角色关系
func (s *SyncService) SyncUserRole(ctx context.Context, userID string) error {
	s.logger.Log("SyncUserRole", fmt.Sprintf("同步用户角色: %s", userID))

	// 清除用户现有角色
	existingRoles := s.manager.GetRolesForUser(userID)
	for _, roleID := range existingRoles {
		if _, err := s.manager.DeleteRoleForUser(userID, roleID); err != nil {
			s.logger.Log("SyncUserRole", fmt.Sprintf("删除用户角色失败: user=%s, role=%s, error=%v", userID, roleID, err))
		}
	}

	// 查询用户的当前角色
	u, err := s.entClient.User.Query().
		Where(user.IDEQ(userID)).
		WithRoles(func(q *ent.RoleQuery) {
			q.Where(role.StatusEQ("active"))
		}).
		Only(ctx)

	if err != nil {
		return fmt.Errorf("查询用户角色失败: %w", err)
	}

	// 重新添加用户角色
	for _, r := range u.Edges.Roles {
		if _, err := s.manager.AddRoleForUser(userID, r.ID); err != nil {
			s.logger.Log("SyncUserRole", fmt.Sprintf("添加用户角色失败: user=%s, role=%s, error=%v", userID, r.ID, err))
		}
	}

	return s.manager.SavePolicy()
}

// SyncRolePermissions 同步单个角色的权限策略
func (s *SyncService) SyncRolePermissions(ctx context.Context, roleID string) error {
	s.logger.Log("SyncRolePermissions", fmt.Sprintf("同步角色权限: %s", roleID))

	// 清除角色现有权限策略
	if err := s.clearRolePolicies(ctx, roleID); err != nil {
		return fmt.Errorf("清除角色现有权限失败: %w", err)
	}

	// 查询角色的当前菜单和API资源
	r, err := s.entClient.Role.Query().
		Where(role.IDEQ(roleID)).
		WithMenus(func(q *ent.MenuQuery) {
			q.Where().
				WithAPIResources(func(ar *ent.ApiResourceQuery) {
					ar.Where(apiresource.IsPublicEQ(false))
				})
		}).
		Only(ctx)

	if err != nil {
		return fmt.Errorf("查询角色权限失败: %w", err)
	}

	// 检查是否为admin角色
	if r.Name == "admin" {
		// admin角色获得通配符权限
		if _, err := s.manager.AddPolicy(roleID, "*", "*"); err != nil {
			s.logger.Log("SyncRolePermissions", fmt.Sprintf("添加admin通配符权限失败: role=%s, error=%v", roleID, err))
		} else {
			s.logger.Log("SyncRolePermissions", fmt.Sprintf("已为admin角色 %s 添加通配符权限", roleID))
		}
	} else {
		// 非admin角色才需要通过菜单和API资源设置权限
		for _, menu := range r.Edges.Menus {
			// 只添加API资源权限
			for _, apiRes := range menu.Edges.APIResources {
				// API资源权限直接使用路径作为对象，不添加前缀
				apiObject := apiRes.Path
				if _, err := s.manager.AddPolicy(roleID, apiObject, apiRes.Method); err != nil {
					s.logger.Log("SyncRolePermissions", fmt.Sprintf("添加API权限失败: role=%s, api=%s:%s, error=%v", roleID, apiRes.Method, apiRes.Path, err))
				}
			}
		}
	}

	return s.manager.SavePolicy()
}

// clearRolePolicies 清除指定角色的所有权限策略
func (s *SyncService) clearRolePolicies(ctx context.Context, roleID string) error {
	policies := s.manager.GetAllPolicies()
	for _, policy := range policies {
		if len(policy) >= 3 && policy[0] == roleID {
			if _, err := s.manager.RemovePolicy(policy[0], policy[1], policy[2]); err != nil {
				s.logger.Log("clearRolePolicies", fmt.Sprintf("移除角色策略失败: %v", err))
			}
		}
	}
	return nil
}

// GetSyncStats 获取同步统计信息
func (s *SyncService) GetSyncStats(ctx context.Context) (*SyncStats, error) {
	stats := &SyncStats{}

	// 统计数据库中的关系数量
	userRoleCount, err := s.entClient.User.Query().
		Where(user.StatusEQ("active")).
		WithRoles(func(q *ent.RoleQuery) {
			q.Where(role.StatusEQ("active"))
		}).
		Count(ctx)
	if err != nil {
		return nil, fmt.Errorf("统计用户角色数量失败: %w", err)
	}

	rolePermCount, err := s.entClient.Role.Query().
		Where(role.StatusEQ("active")).
		WithMenus().
		Count(ctx)
	if err != nil {
		return nil, fmt.Errorf("统计角色权限数量失败: %w", err)
	}

	// 统计casbin中的数据
	casbinRoles := s.manager.GetAllRoles()
	casbinPolicies := s.manager.GetAllPolicies()

	stats.DatabaseUserRoles = userRoleCount
	stats.DatabaseRolePermissions = rolePermCount
	stats.CasbinRoles = len(casbinRoles)
	stats.CasbinPolicies = len(casbinPolicies)

	return stats, nil
}

// SyncStats 同步统计信息
type SyncStats struct {
	DatabaseUserRoles       int `json:"database_user_roles"`
	DatabaseRolePermissions int `json:"database_role_permissions"`
	CasbinRoles             int `json:"casbin_roles"`
	CasbinPolicies          int `json:"casbin_policies"`
}

// IsHealthy 检查同步状态是否健康
func (stats *SyncStats) IsHealthy() bool {
	// 简单的健康检查：casbin中的数据不应该为空
	return stats.CasbinRoles > 0 && stats.CasbinPolicies > 0
}
