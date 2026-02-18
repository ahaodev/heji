package usecase

import (
	"context"
	"fmt"
	"log"
	"shadmin/domain"
	"shadmin/ent"
	entRole "shadmin/ent/role"
	"time"
)

type roleUsecase struct {
	client         *ent.Client
	roleRepository domain.RoleRepository
	contextTimeout time.Duration
}

func (ru *roleUsecase) Create(c context.Context, request *domain.CreateRoleRequest) error {
	ctx, cancel := context.WithTimeout(c, ru.contextTimeout)
	defer cancel()

	// Check if role with same name already exists
	existingRole, err := ru.roleRepository.GetByName(ctx, request.Name)
	if err == nil && existingRole != nil {
		return err
	}

	// Create the role domain model
	role := &domain.Role{
		Name:     request.Name,
		Sequence: request.Sequence,
		Status:   request.Status,
		MenusIds: request.MenuIDs,
	}

	// Set default status if not provided
	if role.Status == "" {
		role.Status = "active"
	}

	// Create the role
	err = ru.roleRepository.Create(ctx, role)
	if err != nil {
		return fmt.Errorf("failed to create role: %w", err)
	}

	// 注意: casbin权限同步由定时任务自动处理，无需在此手动操作
	log.Printf("INFO: Successfully created role %s with %d menus", role.ID, len(role.MenusIds))

	return nil
}

func (ru *roleUsecase) Fetch(c context.Context) ([]*domain.Role, error) {
	ctx, cancel := context.WithTimeout(c, ru.contextTimeout)
	defer cancel()

	return ru.roleRepository.Fetch(ctx)
}

func (ru *roleUsecase) FetchPaged(c context.Context, params domain.QueryParams) (*domain.RolePagedResult, error) {
	ctx, cancel := context.WithTimeout(c, ru.contextTimeout)
	defer cancel()

	return ru.roleRepository.FetchPaged(ctx, params)
}

func (ru *roleUsecase) GetByID(c context.Context, id string) (*domain.Role, error) {
	ctx, cancel := context.WithTimeout(c, ru.contextTimeout)
	defer cancel()

	return ru.roleRepository.GetByID(ctx, id)
}

func (ru *roleUsecase) Update(c context.Context, id string, request *domain.UpdateRoleRequest) (*domain.Role, error) {
	ctx, cancel := context.WithTimeout(c, ru.contextTimeout)
	defer cancel()

	// Get existing role
	existingRole, err := ru.roleRepository.GetByID(ctx, id)
	if err != nil {
		return nil, fmt.Errorf("role not found: %w", err)
	}

	if request.Name != nil {
		existingRole.Name = *request.Name
	}

	if request.Sequence != nil {
		existingRole.Sequence = *request.Sequence
	}

	if request.Status != nil {
		existingRole.Status = *request.Status
	}

	// Handle menu permissions update if provided
	var oldMenuIDs []string
	if existingRole.MenusIds != nil {
		oldMenuIDs = existingRole.MenusIds
	}

	if request.MenuIDs != nil {
		existingRole.MenusIds = request.MenuIDs
	}

	err = ru.roleRepository.Update(ctx, existingRole)
	if err != nil {
		return nil, fmt.Errorf("failed to update role: %w", err)
	}

	// Log menu assignments change - casbin sync handled by scheduled task
	if request.MenuIDs != nil && ru.hasMenuAssignmentsChanged(oldMenuIDs, request.MenuIDs) {
		log.Printf("INFO: Menu assignments changed for role %s, will be synced by scheduled casbin task", existingRole.ID)
	}
	// Fetch and return updated role
	updatedRole, err := ru.roleRepository.GetByID(ctx, id)
	if err != nil {
		return nil, fmt.Errorf("failed to retrieve updated role: %w", err)
	}

	return updatedRole, nil
}

func (ru *roleUsecase) Delete(c context.Context, id string) error {
	ctx, cancel := context.WithTimeout(c, ru.contextTimeout)
	defer cancel()

	// 1. 检查角色是否存在
	role, err := ru.roleRepository.GetByID(ctx, id)
	if err != nil {
		return fmt.Errorf("role not found: %w", err)
	}

	log.Printf("INFO: Starting deletion process for role %s (ID: %s)", role.Name, id)

	// 2. 检查角色是否被用户使用（通过数据库查询）
	userCount, err := ru.client.Role.
		Query().
		Where(entRole.IDEQ(id)).
		QueryUsers().
		Count(ctx)
	if err != nil {
		return fmt.Errorf("failed to check role usage: %w", err)
	}
	if userCount > 0 {
		log.Printf("WARN: Attempted to delete role %s but it is assigned to %d users",
			role.Name, userCount)
		return fmt.Errorf("cannot delete role %s: still assigned to %d users", role.Name, userCount)
	}

	// 3. 在事务中删除角色
	tx, err := ru.client.Tx(ctx)
	if err != nil {
		return fmt.Errorf("failed to begin transaction: %w", err)
	}
	defer tx.Rollback()

	// 删除角色记录
	if err := ru.roleRepository.Delete(ctx, id); err != nil {
		return fmt.Errorf("failed to delete role from database: %w", err)
	}

	// 提交事务
	if err := tx.Commit(); err != nil {
		return fmt.Errorf("failed to commit transaction: %w", err)
	}

	if err != nil {
		log.Printf("ERROR: Failed to delete role %s from database: %v", role.Name, err)
		return err
	}

	// 4. 清理Casbin中的角色策略
	if err := ru.cleanupRolePermissions(role.Name); err != nil {
		// 策略清理失败，记录错误但不回滚角色删除
		log.Printf("ERROR: Failed to clean up permissions for deleted role %s: %v", role.Name, err)
		// 可以考虑加入告警机制
	}

	log.Printf("INFO: Successfully deleted role %s", role.Name)
	return nil
}

// cleanupRolePermissions 记录角色删除，权限清理由定时同步处理
func (ru *roleUsecase) cleanupRolePermissions(roleName string) error {
	log.Printf("INFO: Role %s deleted, permissions will be cleaned up by scheduled casbin sync", roleName)
	return nil
}

// hasMenuAssignmentsChanged 检查菜单分配是否发生变更
func (ru *roleUsecase) hasMenuAssignmentsChanged(oldMenuIDs, newMenuIDs []string) bool {
	if len(oldMenuIDs) != len(newMenuIDs) {
		return true
	}

	// 创建map便于比较
	oldSet := make(map[string]bool)
	for _, menuID := range oldMenuIDs {
		oldSet[menuID] = true
	}

	for _, menuID := range newMenuIDs {
		if !oldSet[menuID] {
			return true
		}
	}

	return false
}

func NewRoleUsecase(client *ent.Client, roleRepository domain.RoleRepository, timeout time.Duration) domain.RoleUseCase {
	return &roleUsecase{
		client:         client,
		roleRepository: roleRepository,
		contextTimeout: timeout,
	}
}
