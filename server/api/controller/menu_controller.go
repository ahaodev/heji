package controller

import (
	"errors"
	"net/http"
	bootstrap "shadmin/bootstrap"
	"shadmin/domain"

	"github.com/gin-gonic/gin"
)

type MenuController struct {
	MenuUseCase domain.MenuUseCase
	Env         *bootstrap.Env
}

// GetMenus 获取菜单列表
// @Summary      Get menus
// @Description  Retrieve a list of menus with filtering and pagination
// @Tags         Menus
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        type      query    string  false  "Filter by menu type (menu/button/api)"
// @Param        status    query    string  false  "Filter by status (active/inactive)"
// @Param        parent_id query    string  false  "Filter by parent menu ID"
// @Param        search    query    string  false  "Search in name or code"
// @Param        page      query    int     false  "Page number (default: 1)"
// @Param        page_size query    int     false  "Page size (default: 20)"
// @Success      200       {object} domain.Response{data=domain.PagedResult[domain.Menu]}  "Successfully retrieved menus"
// @Failure      400       {object} domain.Response  "Bad request - invalid parameters"
// @Failure      500       {object} domain.Response  "Internal server error"
// @Router       /menu [get]
func (mc *MenuController) GetMenus(c *gin.Context) {
	var params domain.MenuQueryParams
	if err := c.ShouldBindQuery(&params); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err.Error()))
		return
	}

	result, err := mc.MenuUseCase.GetMenus(c.Request.Context(), params)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError("Failed to retrieve menus: "+err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess(result))
}

// GetMenuTree 获取菜单树结构
// @Summary      Get menu tree
// @Description  Retrieve menus in hierarchical tree structure
// @Tags         Menus
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        status    query    string  false  "Filter by status (active/inactive)"
// @Success      200       {object} domain.Response{data=[]domain.MenuTreeNode}  "Successfully retrieved menu tree"
// @Failure      400       {object} domain.Response  "Bad request - invalid parameters"
// @Failure      500       {object} domain.Response  "Internal server error"
// @Router       /menu/tree [get]
func (mc *MenuController) GetMenuTree(c *gin.Context) {

	menuTree, err := mc.MenuUseCase.GetMenuTree(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError("Failed to retrieve menu tree: "+err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess(menuTree))
}

// CreateMenu 创建菜单
// @Summary      Create menu
// @Description  Create a new menu item
// @Tags         Menus
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        menu  body     domain.CreateMenuRequest  true  "Menu information"
// @Success      201   {object} domain.Response{data=domain.Menu}  "Successfully created menu"
// @Failure      400   {object} domain.Response  "Bad request - invalid parameters"
// @Failure      409   {object} domain.Response  "Conflict - menu code already exists"
// @Failure      500   {object} domain.Response  "Internal server error"
// @Router       /menu [post]
func (mc *MenuController) CreateMenu(c *gin.Context) {
	var request domain.CreateMenuRequest
	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err.Error()))
		return
	}

	menu, err := mc.MenuUseCase.CreateMenu(c.Request.Context(), &request)
	if err != nil {
		if err == domain.ErrInvalidMenuType {
			c.JSON(http.StatusBadRequest, domain.RespError("Invalid menu type"))
			return
		}
		if err == domain.ErrInvalidMenuStatus {
			c.JSON(http.StatusBadRequest, domain.RespError("Invalid menu status"))
			return
		}
		c.JSON(http.StatusInternalServerError, domain.RespError("Failed to create menu: "+err.Error()))
		return
	}

	c.JSON(http.StatusCreated, domain.RespSuccess(menu))
}

// GetMenu 获取单个菜单
// @Summary      Get menu by ID
// @Description  Get a specific menu by its ID
// @Tags         Menus
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        id   path     string  true  "Menu ID"
// @Success      200  {object} domain.Response{data=domain.Menu}  "Successfully retrieved menu"
// @Failure      404  {object} domain.Response  "Menu not found"
// @Failure      500  {object} domain.Response  "Internal server error"
// @Router       /menu/{id} [get]
func (mc *MenuController) GetMenu(c *gin.Context) {
	id := c.Param("id")

	menu, err := mc.MenuUseCase.GetMenuByID(c.Request.Context(), id)
	if err != nil {
		c.JSON(http.StatusNotFound, domain.RespError("Menu not found"))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess(menu))
}

// UpdateMenu 更新菜单
// @Summary      Update menu
// @Description  Update a specific menu by its ID
// @Tags         Menus
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        id    path     string                    true  "Menu ID"
// @Param        menu  body     domain.UpdateMenuRequest  true  "Updated menu information"
// @Success      200   {object} domain.Response{data=domain.Menu}  "Successfully updated menu"
// @Failure      400   {object} domain.Response  "Bad request - invalid parameters"
// @Failure      404   {object} domain.Response  "Menu not found"
// @Failure      500   {object} domain.Response  "Internal server error"
// @Router       /menu/{id} [put]
func (mc *MenuController) UpdateMenu(c *gin.Context) {
	id := c.Param("id")
	var request domain.UpdateMenuRequest
	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err.Error()))
		return
	}

	menu, err := mc.MenuUseCase.UpdateMenu(c.Request.Context(), id, &request)
	if err != nil {
		if errors.Is(err, domain.ErrInvalidMenuType) {
			c.JSON(http.StatusBadRequest, domain.RespError("Invalid menu type"))
			return
		}
		if errors.Is(err, domain.ErrInvalidMenuStatus) {
			c.JSON(http.StatusBadRequest, domain.RespError("Invalid menu status"))
			return
		}
		if errors.Is(err, domain.ErrMenuNotFound) {
			c.JSON(http.StatusNotFound, domain.RespError("Menu not found"))
			return
		}
		c.JSON(http.StatusInternalServerError, domain.RespError("Failed to update menu: "+err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess(menu))
}

// DeleteMenu 删除菜单
// @Summary      Delete menu
// @Description  Delete a specific menu by its ID
// @Tags         Menus
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        id   path     string  true  "Menu ID"
// @Success      200  {object} domain.Response  "Successfully deleted menu"
// @Failure      404  {object} domain.Response  "Menu not found"
// @Failure      500  {object} domain.Response  "Internal server error"
// @Router       /menu/{id} [delete]
func (mc *MenuController) DeleteMenu(c *gin.Context) {
	id := c.Param("id")

	err := mc.MenuUseCase.DeleteMenu(c.Request.Context(), id)
	if err != nil {
		if err == domain.ErrMenuNotFound {
			c.JSON(http.StatusNotFound, domain.RespError("Menu not found"))
			return
		}
		if err == domain.ErrMenuHasChildren {
			c.JSON(http.StatusBadRequest, domain.RespError("Cannot delete menu with children"))
			return
		}
		c.JSON(http.StatusInternalServerError, domain.RespError("Failed to delete menu: "+err.Error()))
		return
	}

	response := map[string]interface{}{
		"menu_id": id,
		"message": "Menu deleted successfully",
	}
	c.JSON(http.StatusOK, domain.RespSuccess(response))
}
