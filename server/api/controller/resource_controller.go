package controller

import (
	"net/http"
	bootstrap "shadmin/bootstrap"
	"shadmin/domain"
	"shadmin/internal/constants"
	"shadmin/pkg"

	"github.com/gin-gonic/gin"
)

type ResourceController struct {
	MenuRepository domain.MenuRepository
	UserRepository domain.UserRepository
	RoleRepository domain.RoleRepository
	Env            *bootstrap.Env
}

// GetResources 获取用户可访问的系统资源菜单 (基于RBAC权限过滤)
// @Summary      Get user accessible resources
// @Description  Retrieve menu tree and button permissions filtered by user permissions
// @Tags         Resources
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Success      200       {object} domain.Response{data=object{menus=[]domain.MenuTreeNode,permissions=[]string}}  "Successfully retrieved user resources with permissions"
// @Failure      500       {object} domain.Response  "Internal server error"
// @Router       /resources [get]
func (rc *ResourceController) GetResources(c *gin.Context) {
	userID := c.GetString(constants.UserID)
	isAdmin := c.GetBool(constants.IsAdmin)

	// 获取所有菜单
	menuTree, err := rc.MenuRepository.GetMenuTree(c.Request.Context())
	if err != nil {
		pkg.Log.WithField("userID", userID).WithError(err).Error("Failed to retrieve menu tree")
		c.JSON(http.StatusInternalServerError, domain.RespError("Failed to retrieve resources: "+err.Error()))
		return
	}

	// 管理员直接返回所有菜单和权限
	if isAdmin {
		allPermissions := rc.collectAllPermissions(menuTree)
		responseData := gin.H{
			"menus":       menuTree,
			"permissions": allPermissions,
		}
		c.JSON(http.StatusOK, domain.RespSuccess(responseData))
		return
	}

	// 获取用户信息和角色权限
	user, err := rc.UserRepository.GetByID(c, userID)
	if err != nil {
		pkg.Log.WithField("userID", userID).WithError(err).Error("Failed to get user information")
		c.JSON(http.StatusInternalServerError, domain.RespError("Failed to get user: "+err.Error()))
		return
	}

	// 批量获取用户角色
	roles, err := rc.getUserRoles(c, user.Roles)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError("Failed to get user roles: "+err.Error()))
		return
	}

	// 收集用户菜单ID和权限
	userMenuIDs, userPermissions := rc.collectUserPermissions(c, roles)

	// 过滤菜单树
	filteredMenuTree := rc.filterMenuTree(menuTree, userMenuIDs)

	// 构建响应数据
	responseData := gin.H{
		"menus":       filteredMenuTree,
		"permissions": userPermissions,
	}
	c.JSON(http.StatusOK, domain.RespSuccess(responseData))
}

// collectAllPermissions 收集所有按钮权限
func (rc *ResourceController) collectAllPermissions(menuTree []domain.MenuTreeNode) []string {
	var permissions []string
	permissionMap := make(map[string]bool)

	var collect func([]domain.MenuTreeNode)
	collect = func(nodes []domain.MenuTreeNode) {
		for _, node := range nodes {
			if node.Type == domain.MenuTypeButton && node.Permissions != nil && *node.Permissions != "" {
				if !permissionMap[*node.Permissions] {
					permissionMap[*node.Permissions] = true
					permissions = append(permissions, *node.Permissions)
				}
			}
			collect(node.Children)
		}
	}
	collect(menuTree)
	return permissions
}

// getUserRoles 批量获取用户角色信息
func (rc *ResourceController) getUserRoles(c *gin.Context, roleIDs []string) ([]*domain.Role, error) {
	var roles []*domain.Role
	var failedRoles []string

	for _, roleID := range roleIDs {
		role, err := rc.RoleRepository.GetByID(c, roleID)
		if err != nil {
			failedRoles = append(failedRoles, roleID)
			pkg.Log.WithField("roleID", roleID).WithError(err).Warn("Failed to get role information")
			continue
		}
		roles = append(roles, role)
	}

	if len(failedRoles) > 0 {
		pkg.Log.WithField("failedRoles", failedRoles).Warn("Some roles could not be retrieved")
	}

	return roles, nil
}

// collectUserPermissions 收集用户菜单ID和权限
func (rc *ResourceController) collectUserPermissions(c *gin.Context, roles []*domain.Role) ([]string, []string) {
	var userMenuIDs []string
	var userPermissions []string
	permissionMap := make(map[string]bool)

	// 收集所有菜单ID
	for _, role := range roles {
		userMenuIDs = append(userMenuIDs, role.MenusIds...)
	}

	// 收集权限标识
	var failedMenus []string
	for _, role := range roles {
		for _, menuID := range role.MenusIds {
			menu, err := rc.MenuRepository.GetMenuByID(c, menuID)
			if err != nil {
				failedMenus = append(failedMenus, menuID)
				pkg.Log.WithField("menuID", menuID).WithError(err).Warn("Failed to get menu information")
				continue
			}
			if menu.Type == domain.MenuTypeButton && menu.Permissions != nil && *menu.Permissions != "" {
				if !permissionMap[*menu.Permissions] {
					permissionMap[*menu.Permissions] = true
					userPermissions = append(userPermissions, *menu.Permissions)
				}
			}
		}
	}

	if len(failedMenus) > 0 {
		pkg.Log.WithField("failedMenus", failedMenus).Warn("Some menus could not be retrieved for permission collection")
	}

	return userMenuIDs, userPermissions
}

// filterMenuTree 过滤菜单树
func (rc *ResourceController) filterMenuTree(menuTree []domain.MenuTreeNode, userMenuIDs []string) []domain.MenuTreeNode {
	allowedIDsMap := make(map[string]bool)
	for _, id := range userMenuIDs {
		allowedIDsMap[id] = true
	}

	var filteredMenus []domain.MenuTreeNode
	for _, menu := range menuTree {
		if filteredMenu := rc.filterMenuNodeByIDs(menu, allowedIDsMap); filteredMenu != nil {
			filteredMenus = append(filteredMenus, *filteredMenu)
		}
	}
	return filteredMenus
}

// filterMenuNodeByIDs 递归过滤单个菜单节点及其子节点
func (rc *ResourceController) filterMenuNodeByIDs(menu domain.MenuTreeNode, allowedIDsMap map[string]bool) *domain.MenuTreeNode {
	// 递归过滤子菜单
	var filteredChildren []domain.MenuTreeNode
	for _, child := range menu.Children {
		if filteredChild := rc.filterMenuNodeByIDs(child, allowedIDsMap); filteredChild != nil {
			filteredChildren = append(filteredChildren, *filteredChild)
		}
	}

	// 如果当前菜单有权限或者有有权限的子菜单，则保留
	hasPermission := allowedIDsMap[menu.ID]
	hasAccessibleChildren := len(filteredChildren) > 0

	if hasPermission || hasAccessibleChildren {
		filteredMenu := menu
		filteredMenu.Children = filteredChildren
		return &filteredMenu
	}

	return nil
}
