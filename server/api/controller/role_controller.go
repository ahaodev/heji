package controller

import (
	"net/http"
	bootstrap "shadmin/bootstrap"
	"shadmin/domain"
	"shadmin/internal/casbin"

	"github.com/gin-gonic/gin"
)

type RoleController struct {
	CasManager     casbin.Manager
	Env            *bootstrap.Env
	RoleUseCase    domain.RoleUseCase
	UserRepository domain.UserRepository
	RoleRepository domain.RoleRepository
	MenuRepository domain.MenuRepository
}

// RoleInfo 角色信息结构
type RoleInfo struct {
	ID   string `json:"id"`   // 角色ID
	Name string `json:"name"` // 角色显示名称
	Type string `json:"type"` // 角色类型
}

// GetRoles 获取所有角色
// @Summary      Get all roles
// @Description  Retrieve a list of all available roles in the system
// @Tags         Roles
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        search    query    string  false  "Search roles by name"
// @Success      200  {object} domain.Response{data=[]RoleInfo}  "Successfully retrieved roles"
// @Failure      500  {object} domain.Response  "Internal server error"
// @Router       /system/role [get]
func (rc *RoleController) GetRoles(c *gin.Context) {
	// 获取所有角色
	roles, err := rc.RoleUseCase.Fetch(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	var allRoles []RoleInfo
	for _, role := range roles {
		if role.Status == "active" {
			allRoles = append(allRoles, RoleInfo{
				ID:   role.ID,
				Name: role.Name,
				Type: "role",
			})
		}
	}

	c.JSON(http.StatusOK, domain.RespSuccess(allRoles))
}

// CreateRole 创建自定义角色
// @Summary      Create a new custom role
// @Description  Create a new custom role in the system
// @Tags         Roles
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        request  body     domain.CreateRoleRequest  true  "Role creation information"
// @Success      201      {object} domain.Response{data=domain.Role}  "Successfully created role"
// @Failure      400      {object} domain.Response  "Bad request - invalid parameters"
// @Failure      409      {object} domain.Response  "Role code already exists"
// @Failure      500      {object} domain.Response  "Internal server error"
// @Router       /system/role [post]
func (rc *RoleController) CreateRole(c *gin.Context) {
	var request domain.CreateRoleRequest
	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err.Error()))
		return
	}

	// Set default status if not provided
	if request.Status == "" {
		request.Status = "active"
	}

	// Create the role
	err := rc.RoleUseCase.Create(c.Request.Context(), &request)
	if err != nil {
		if err.Error() == "role code already exists" {
			c.JSON(http.StatusConflict, domain.RespError(err.Error()))
			return
		}
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusCreated, domain.RespSuccess(err))
}

// GetRole 获取角色详情
// @Summary      Get role by ID
// @Description  Get a specific role by its ID
// @Tags         Roles
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        id   path     string  true  "Role ID"
// @Success      200  {object} domain.Response{data=domain.Role}  "Successfully retrieved role"
// @Failure      404  {object} domain.Response  "Role not found"
// @Failure      500  {object} domain.Response  "Internal server error"
// @Router       /system/role/{id} [get]
func (rc *RoleController) GetRole(c *gin.Context) {
	roleID := c.Param("id")
	if roleID == "" {
		c.JSON(http.StatusBadRequest, domain.RespError("Role ID is required"))
		return
	}

	role, err := rc.RoleUseCase.GetByID(c.Request.Context(), roleID)
	if err != nil {
		if err.Error() == "role not found" {
			c.JSON(http.StatusNotFound, domain.RespError(err.Error()))
			return
		}
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess(role))
}

// UpdateRole 更新角色
// @Summary      Update role
// @Description  Update an existing role
// @Tags         Roles
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        id       path     string                     true  "Role ID"
// @Param        request  body     domain.UpdateRoleRequest   true  "Role update information"
// @Success      200      {object} domain.Response{data=domain.Role}  "Successfully updated role"
// @Failure      400      {object} domain.Response  "Bad request - invalid parameters"
// @Failure      404      {object} domain.Response  "Role not found"
// @Failure      409      {object} domain.Response  "Role code already exists"
// @Failure      500      {object} domain.Response  "Internal server error"
// @Router       /system/role/{id} [put]
func (rc *RoleController) UpdateRole(c *gin.Context) {
	roleID := c.Param("id")
	if roleID == "" {
		c.JSON(http.StatusBadRequest, domain.RespError("Role ID is required"))
		return
	}

	var request domain.UpdateRoleRequest
	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err.Error()))
		return
	}

	role, err := rc.RoleUseCase.Update(c.Request.Context(), roleID, &request)
	if err != nil {
		if err.Error() == "role not found" {
			c.JSON(http.StatusNotFound, domain.RespError(err.Error()))
			return
		}
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess(role))
}

// DeleteRole 删除角色
// @Summary      Delete role
// @Description  Delete an existing role
// @Tags         Roles
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        id   path     string  true  "Role ID"
// @Success      200  {object} domain.Response  "Successfully deleted role"
// @Failure      404  {object} domain.Response  "Role not found"
// @Failure      500  {object} domain.Response  "Internal server error"
// @Router       /system/role/{id} [delete]
func (rc *RoleController) DeleteRole(c *gin.Context) {
	roleID := c.Param("id")
	if roleID == "" {
		c.JSON(http.StatusBadRequest, domain.RespError("Role ID is required"))
		return
	}

	// 删除数据库中的角色
	err := rc.RoleUseCase.Delete(c.Request.Context(), roleID)
	if err != nil {
		if err.Error() == "role not found" {
			c.JSON(http.StatusNotFound, domain.RespError(err.Error()))
			return
		}
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	// 注意：由于使用集中同步模式，角色关系会在下次同步时自动清理
	// 不需要手动删除 Casbin 中的角色分配
	c.JSON(http.StatusOK, domain.RespSuccess("Role deleted successfully"))
}

// GetRoleMenus 获取角色的菜单权限
// @Summary      Get role menu permissions
// @Description  Get menu permissions assigned to a role
// @Tags         Roles
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        id   path     string  true  "Role ID"
// @Success      200  {object} domain.Response{data=[]string}  "Successfully retrieved role menu permissions"
// @Failure      400  {object} domain.Response  "Bad request - Role ID is required"
// @Failure      404  {object} domain.Response  "Role not found"
// @Failure      500  {object} domain.Response  "Internal server error"
// @Router       /system/role/menus/{id} [get]
func (rc *RoleController) GetRoleMenus(c *gin.Context) {
	roleID := c.Param("id")
	if roleID == "" {
		c.JSON(http.StatusBadRequest, domain.RespError("Role ID is required"))
		return
	}
	role, err := rc.RoleUseCase.GetByID(c.Request.Context(), roleID)
	if err != nil {
		if err.Error() == "role not found" {
			c.JSON(http.StatusNotFound, domain.RespError(err.Error()))
			return
		}
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess(role.MenusIds))
}
