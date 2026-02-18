package permission

import (
	"context"
	"fmt"
	"shadmin/internal/casbin"
	"strings"
)

// PermissionIdentifierService 权限标识服务
// 管理存储在Casbin中的权限标识，用于前端权限控制
type PermissionIdentifierService struct {
	casManager casbin.Manager
}

// 权限标识资源类型
const (
	IdentifierMenu    = "menu"    // 菜单权限：menu:用户管理:access
	IdentifierButton  = "button"  // 按钮权限：button:添加用户:access
	IdentifierModule  = "module"  // 模块权限：module:财务:access
	IdentifierProject = "project" // 项目权限：project:项目A:read
)

// 权限标识操作
const (
	IdentifierActionAccess = "access" // 访问权限
	IdentifierActionRead   = "read"   // 读权限
	IdentifierActionWrite  = "write"  // 写权限
	IdentifierActionDelete = "delete" // 删除权限
)

// NewPermissionIdentifierService 创建权限标识服务
func NewPermissionIdentifierService(casManager casbin.Manager) *PermissionIdentifierService {
	return &PermissionIdentifierService{
		casManager: casManager,
	}
}

// GetPermissionIdentifiersForRole 获取角色的所有权限标识
func (pis *PermissionIdentifierService) GetPermissionIdentifiersForRole(ctx context.Context, role string) ([]string, error) {
	// 获取所有策略并过滤出当前角色的权限
	allPolicies := pis.casManager.GetAllPolicies()
	var policies [][]string
	for _, policy := range allPolicies {
		if len(policy) >= 3 && policy[0] == role {
			policies = append(policies, policy)
		}
	}

	var identifiers []string
	for _, policy := range policies {
		if len(policy) >= 3 {
			object := policy[1]
			action := policy[2]

			// 只处理权限标识（不包含API路径）
			if pis.isPermissionIdentifier(object) {
				identifier := fmt.Sprintf("%s:%s", object, action)
				identifiers = append(identifiers, identifier)
			}
		}
	}

	return identifiers, nil
}

// GetPermissionIdentifiersForUser 获取用户的所有权限标识（通过角色）
func (pis *PermissionIdentifierService) GetPermissionIdentifiersForUser(ctx context.Context, username string) ([]string, error) {
	// 获取用户角色
	roles := pis.casManager.GetRolesForUser(username)
	if len(roles) == 0 {
		return []string{}, nil
	}

	// 收集所有角色的权限标识
	var allIdentifiers []string
	identifierSet := make(map[string]bool) // 去重

	for _, role := range roles {
		roleIdentifiers, err := pis.GetPermissionIdentifiersForRole(ctx, role)
		if err != nil {
			return nil, fmt.Errorf("failed to get identifiers for role %s: %w", role, err)
		}

		// 去重添加
		for _, identifier := range roleIdentifiers {
			if !identifierSet[identifier] {
				identifierSet[identifier] = true
				allIdentifiers = append(allIdentifiers, identifier)
			}
		}
	}

	return allIdentifiers, nil
}

// UserHasPermissionIdentifier 检查用户是否拥有指定权限标识
func (pis *PermissionIdentifierService) UserHasPermissionIdentifier(ctx context.Context, username string, identifier string) (bool, error) {
	if err := pis.validateIdentifier(identifier); err != nil {
		return false, err
	}

	resourceType, resourceName, action := pis.parseIdentifier(identifier)
	object := fmt.Sprintf("%s:%s", resourceType, resourceName)

	return pis.casManager.CheckPermission(username, object, action)
}

// UserHasAnyPermissionIdentifier 检查用户是否拥有任一权限标识
func (pis *PermissionIdentifierService) UserHasAnyPermissionIdentifier(ctx context.Context, username string, identifiers []string) (bool, error) {
	for _, identifier := range identifiers {
		hasPermission, err := pis.UserHasPermissionIdentifier(ctx, username, identifier)
		if err != nil {
			return false, err
		}
		if hasPermission {
			return true, nil
		}
	}
	return false, nil
}

// ClearPermissionIdentifiersForRole 清除角色的所有权限标识
func (pis *PermissionIdentifierService) ClearPermissionIdentifiersForRole(ctx context.Context, role string) error {
	// 获取所有策略并过滤出当前角色的权限
	allPolicies := pis.casManager.GetAllPolicies()
	var policies [][]string
	for _, policy := range allPolicies {
		if len(policy) >= 3 && policy[0] == role {
			policies = append(policies, policy)
		}
	}

	for _, policy := range policies {
		if len(policy) >= 3 {
			object := policy[1]
			action := policy[2]

			// 只删除权限标识（不删除API权限）
			if pis.isPermissionIdentifier(object) {
				if _, err := pis.casManager.RemovePolicy(role, object, action); err != nil {
					return fmt.Errorf("failed to remove identifier policy: %w", err)
				}
			}
		}
	}

	return nil
}

// ========== 分类获取权限标识 ==========

// GetMenuPermissionsForUser 获取用户的菜单权限标识
func (pis *PermissionIdentifierService) GetMenuPermissionsForUser(ctx context.Context, username string) ([]string, error) {
	return pis.getPermissionsByTypeForUser(ctx, username, IdentifierMenu)
}

// GetButtonPermissionsForUser 获取用户的按钮权限标识
func (pis *PermissionIdentifierService) GetButtonPermissionsForUser(ctx context.Context, username string) ([]string, error) {
	return pis.getPermissionsByTypeForUser(ctx, username, IdentifierButton)
}

// GetModulePermissionsForUser 获取用户的模块权限标识
func (pis *PermissionIdentifierService) GetModulePermissionsForUser(ctx context.Context, username string) ([]string, error) {
	return pis.getPermissionsByTypeForUser(ctx, username, IdentifierModule)
}

// getPermissionsByTypeForUser 获取用户指定类型的权限标识（内部方法）
func (pis *PermissionIdentifierService) getPermissionsByTypeForUser(ctx context.Context, username string, resourceType string) ([]string, error) {
	allIdentifiers, err := pis.GetPermissionIdentifiersForUser(ctx, username)
	if err != nil {
		return nil, err
	}

	var filteredIdentifiers []string
	prefix := resourceType + ":"

	for _, identifier := range allIdentifiers {
		if strings.HasPrefix(identifier, prefix) {
			filteredIdentifiers = append(filteredIdentifiers, identifier)
		}
	}

	return filteredIdentifiers, nil
}

// ========== 辅助方法 ==========

// validateIdentifier 验证权限标识格式
func (pis *PermissionIdentifierService) validateIdentifier(identifier string) error {
	parts := strings.Split(identifier, ":")
	if len(parts) != 3 {
		return fmt.Errorf("invalid identifier format, expected 'type:name:action', got: %s", identifier)
	}

	resourceType := parts[0]
	validTypes := map[string]bool{
		IdentifierMenu:    true,
		IdentifierButton:  true,
		IdentifierModule:  true,
		IdentifierProject: true,
	}

	if !validTypes[resourceType] {
		return fmt.Errorf("invalid resource type: %s", resourceType)
	}

	return nil
}

// parseIdentifier 解析权限标识
func (pis *PermissionIdentifierService) parseIdentifier(identifier string) (resourceType, resourceName, action string) {
	parts := strings.Split(identifier, ":")
	if len(parts) == 3 {
		return parts[0], parts[1], parts[2]
	}
	return "", "", ""
}

// isPermissionIdentifier 判断是否为权限标识（而不是API路径）
func (pis *PermissionIdentifierService) isPermissionIdentifier(object string) bool {
	// 权限标识格式：type:name，API路径格式：/api/...
	return !strings.HasPrefix(object, "/") && strings.Contains(object, ":")
}

// BuildIdentifier 构建权限标识
func (pis *PermissionIdentifierService) BuildIdentifier(resourceType, resourceName, action string) string {
	return fmt.Sprintf("%s:%s:%s", resourceType, resourceName, action)
}
