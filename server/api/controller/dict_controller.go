package controller

import (
	"net/http"
	"shadmin/domain"
	"strconv"

	"github.com/gin-gonic/gin"
)

type DictController struct {
	DictUseCase domain.DictUseCase
}

// 字典类型相关接口

// GetDictTypes godoc
// @Summary      Get dictionary types with pagination
// @Description  Retrieve dictionary types with optional filtering and pagination
// @Tags         Dictionary
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        page      query  int     false  "Page number (default: 1)"
// @Param        page_size query  int     false  "Page size (default: 10)"
// @Param        code      query  string  false  "Filter by type code"
// @Param        name      query  string  false  "Filter by type name"
// @Param        status    query  string  false  "Filter by status (active, inactive)"
// @Param        search    query  string  false  "Search by code or name"
// @Param        sort_by   query  string  false  "Sort by field (code, name, created_at)"
// @Param        order     query  string  false  "Sort order (asc, desc)"
// @Success      200       {object}  domain.Response{data=domain.DictTypePagedResult}  "Dictionary types retrieved successfully"
// @Failure      500       {object}  domain.Response  "Internal server error"
// @Router       /system/dict/types [get]
func (dc *DictController) GetDictTypes(c *gin.Context) {
	var params domain.DictTypeQueryParams

	// 解析分页参数
	if p := c.Query("page"); p != "" {
		if v, err := strconv.Atoi(p); err == nil {
			params.Page = v
		}
	}
	if ps := c.Query("page_size"); ps != "" {
		if v, err := strconv.Atoi(ps); err == nil {
			params.PageSize = v
		}
	}

	// 解析过滤参数
	params.Code = c.Query("code")
	params.Name = c.Query("name")
	params.Status = c.Query("status")
	params.Search = c.Query("search")
	params.SortBy = c.Query("sort_by")
	params.Order = c.Query("order")

	result, err := dc.DictUseCase.ListDictTypes(c, params)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess(result))
}

// GetDictType godoc
// @Summary      Get dictionary type by ID
// @Description  Retrieve a dictionary type by its ID
// @Tags         Dictionary
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        id   path      string  true  "Dictionary type ID"
// @Success      200  {object}  domain.Response{data=domain.DictType}  "Dictionary type retrieved successfully"
// @Failure      404  {object}  domain.Response  "Dictionary type not found"
// @Failure      500  {object}  domain.Response  "Internal server error"
// @Router       /system/dict/types/{id} [get]
func (dc *DictController) GetDictType(c *gin.Context) {
	id := c.Param("id")

	dictType, err := dc.DictUseCase.GetDictTypeByID(c, id)
	if err != nil {
		if err == domain.ErrDictTypeNotFound {
			c.JSON(http.StatusNotFound, domain.RespError("Dictionary type not found"))
			return
		}
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess(dictType))
}

// CreateDictType godoc
// @Summary      Create a new dictionary type
// @Description  Create a new dictionary type
// @Tags         Dictionary
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        request  body      domain.CreateDictTypeRequest  true  "Dictionary type creation data"
// @Success      201      {object}  domain.Response{data=domain.DictType}  "Dictionary type created successfully"
// @Failure      400      {object}  domain.Response  "Invalid request data"
// @Failure      409      {object}  domain.Response  "Dictionary type code already exists"
// @Failure      500      {object}  domain.Response  "Internal server error"
// @Router       /system/dict/types [post]
func (dc *DictController) CreateDictType(c *gin.Context) {
	var request domain.CreateDictTypeRequest
	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err.Error()))
		return
	}

	dictType, err := dc.DictUseCase.CreateDictType(c, &request)
	if err != nil {
		if err == domain.ErrDictTypeCodeExists {
			c.JSON(http.StatusConflict, domain.RespError("Dictionary type code already exists"))
			return
		}
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusCreated, domain.RespSuccess(dictType))
}

// UpdateDictType godoc
// @Summary      Update dictionary type
// @Description  Update a dictionary type by ID
// @Tags         Dictionary
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        id       path      string                        true   "Dictionary type ID"
// @Param        request  body      domain.UpdateDictTypeRequest  true   "Dictionary type update data"
// @Success      200      {object}  domain.Response  "Dictionary type updated successfully"
// @Failure      400      {object}  domain.Response  "Invalid request data"
// @Failure      404      {object}  domain.Response  "Dictionary type not found"
// @Failure      409      {object}  domain.Response  "Dictionary type code already exists"
// @Failure      500      {object}  domain.Response  "Internal server error"
// @Router       /system/dict/types/{id} [put]
func (dc *DictController) UpdateDictType(c *gin.Context) {
	id := c.Param("id")

	var request domain.UpdateDictTypeRequest
	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err.Error()))
		return
	}

	err := dc.DictUseCase.UpdateDictType(c, id, request)
	if err != nil {
		if err == domain.ErrDictTypeNotFound {
			c.JSON(http.StatusNotFound, domain.RespError("Dictionary type not found"))
			return
		}
		if err == domain.ErrDictTypeCodeExists {
			c.JSON(http.StatusConflict, domain.RespError("Dictionary type code already exists"))
			return
		}
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess("Dictionary type updated successfully"))
}

// DeleteDictType godoc
// @Summary      Delete dictionary type
// @Description  Delete a dictionary type by ID
// @Tags         Dictionary
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        id   path      string  true  "Dictionary type ID"
// @Success      200  {object}  domain.Response  "Dictionary type deleted successfully"
// @Failure      400  {object}  domain.Response  "Dictionary type has items, cannot delete"
// @Failure      404  {object}  domain.Response  "Dictionary type not found"
// @Failure      500  {object}  domain.Response  "Internal server error"
// @Router       /system/dict/types/{id} [delete]
func (dc *DictController) DeleteDictType(c *gin.Context) {
	id := c.Param("id")

	err := dc.DictUseCase.DeleteDictType(c, id)
	if err != nil {
		if err == domain.ErrDictTypeNotFound {
			c.JSON(http.StatusNotFound, domain.RespError("Dictionary type not found"))
			return
		}
		if err == domain.ErrDictTypeHasItems {
			c.JSON(http.StatusBadRequest, domain.RespError("Dictionary type has items, cannot delete"))
			return
		}
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess("Dictionary type deleted successfully"))
}

// 字典项相关接口

// GetDictItems godoc
// @Summary      Get dictionary items with pagination
// @Description  Retrieve dictionary items with optional filtering and pagination
// @Tags         Dictionary
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        page      query  int     false  "Page number (default: 1)"
// @Param        page_size query  int     false  "Page size (default: 10)"
// @Param        type_id   query  string  false  "Filter by dictionary type ID"
// @Param        type_code query  string  false  "Filter by dictionary type code"
// @Param        label     query  string  false  "Filter by item label"
// @Param        value     query  string  false  "Filter by item value"
// @Param        status    query  string  false  "Filter by status (active, inactive)"
// @Param        search    query  string  false  "Search by label or value"
// @Param        sort_by   query  string  false  "Sort by field (label, value, sort, created_at)"
// @Param        order     query  string  false  "Sort order (asc, desc)"
// @Success      200       {object}  domain.Response{data=domain.DictItemPagedResult}  "Dictionary items retrieved successfully"
// @Failure      500       {object}  domain.Response  "Internal server error"
// @Router       /system/dict/items [get]
func (dc *DictController) GetDictItems(c *gin.Context) {
	var params domain.DictItemQueryParams

	// 解析分页参数
	if p := c.Query("page"); p != "" {
		if v, err := strconv.Atoi(p); err == nil {
			params.Page = v
		}
	}
	if ps := c.Query("page_size"); ps != "" {
		if v, err := strconv.Atoi(ps); err == nil {
			params.PageSize = v
		}
	}

	// 解析过滤参数
	params.TypeID = c.Query("type_id")
	params.TypeCode = c.Query("type_code")
	params.Label = c.Query("label")
	params.Value = c.Query("value")
	params.Status = c.Query("status")
	params.Search = c.Query("search")
	params.SortBy = c.Query("sort_by")
	params.Order = c.Query("order")

	result, err := dc.DictUseCase.ListDictItems(c, params)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess(result))
}

// GetDictItem godoc
// @Summary      Get dictionary item by ID
// @Description  Retrieve a dictionary item by its ID
// @Tags         Dictionary
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        id   path      string  true  "Dictionary item ID"
// @Success      200  {object}  domain.Response{data=domain.DictItem}  "Dictionary item retrieved successfully"
// @Failure      404  {object}  domain.Response  "Dictionary item not found"
// @Failure      500  {object}  domain.Response  "Internal server error"
// @Router       /system/dict/items/{id} [get]
func (dc *DictController) GetDictItem(c *gin.Context) {
	id := c.Param("id")

	dictItem, err := dc.DictUseCase.GetDictItemByID(c, id)
	if err != nil {
		if err == domain.ErrDictItemNotFound {
			c.JSON(http.StatusNotFound, domain.RespError("Dictionary item not found"))
			return
		}
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess(dictItem))
}

// CreateDictItem godoc
// @Summary      Create a new dictionary item
// @Description  Create a new dictionary item
// @Tags         Dictionary
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        request  body      domain.CreateDictItemRequest  true  "Dictionary item creation data"
// @Success      201      {object}  domain.Response{data=domain.DictItem}  "Dictionary item created successfully"
// @Failure      400      {object}  domain.Response  "Invalid request data"
// @Failure      409      {object}  domain.Response  "Dictionary item value already exists or default conflict"
// @Failure      500      {object}  domain.Response  "Internal server error"
// @Router       /system/dict/items [post]
func (dc *DictController) CreateDictItem(c *gin.Context) {
	var request domain.CreateDictItemRequest
	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err.Error()))
		return
	}

	dictItem, err := dc.DictUseCase.CreateDictItem(c, &request)
	if err != nil {
		if err == domain.ErrDictItemValueExists {
			c.JSON(http.StatusConflict, domain.RespError("Dictionary item value already exists in this type"))
			return
		}
		if err == domain.ErrDictItemDefaultConflict {
			c.JSON(http.StatusConflict, domain.RespError("Dictionary type already has a default item"))
			return
		}
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusCreated, domain.RespSuccess(dictItem))
}

// UpdateDictItem godoc
// @Summary      Update dictionary item
// @Description  Update a dictionary item by ID
// @Tags         Dictionary
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        id       path      string                        true   "Dictionary item ID"
// @Param        request  body      domain.UpdateDictItemRequest  true   "Dictionary item update data"
// @Success      200      {object}  domain.Response  "Dictionary item updated successfully"
// @Failure      400      {object}  domain.Response  "Invalid request data"
// @Failure      404      {object}  domain.Response  "Dictionary item not found"
// @Failure      409      {object}  domain.Response  "Dictionary item value already exists or default conflict"
// @Failure      500      {object}  domain.Response  "Internal server error"
// @Router       /system/dict/items/{id} [put]
func (dc *DictController) UpdateDictItem(c *gin.Context) {
	id := c.Param("id")

	var request domain.UpdateDictItemRequest
	if err := c.ShouldBindJSON(&request); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err.Error()))
		return
	}

	err := dc.DictUseCase.UpdateDictItem(c, id, request)
	if err != nil {
		if err == domain.ErrDictItemNotFound {
			c.JSON(http.StatusNotFound, domain.RespError("Dictionary item not found"))
			return
		}
		if err == domain.ErrDictItemValueExists {
			c.JSON(http.StatusConflict, domain.RespError("Dictionary item value already exists in this type"))
			return
		}
		if err == domain.ErrDictItemDefaultConflict {
			c.JSON(http.StatusConflict, domain.RespError("Dictionary type already has a default item"))
			return
		}
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess("Dictionary item updated successfully"))
}

// DeleteDictItem godoc
// @Summary      Delete dictionary item
// @Description  Delete a dictionary item by ID
// @Tags         Dictionary
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        id   path      string  true  "Dictionary item ID"
// @Success      200  {object}  domain.Response  "Dictionary item deleted successfully"
// @Failure      404  {object}  domain.Response  "Dictionary item not found"
// @Failure      500  {object}  domain.Response  "Internal server error"
// @Router       /system/dict/items/{id} [delete]
func (dc *DictController) DeleteDictItem(c *gin.Context) {
	id := c.Param("id")

	err := dc.DictUseCase.DeleteDictItem(c, id)
	if err != nil {
		if err == domain.ErrDictItemNotFound {
			c.JSON(http.StatusNotFound, domain.RespError("Dictionary item not found"))
			return
		}
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess("Dictionary item deleted successfully"))
}

// 便捷查询接口

// GetDictItemsByTypeCode godoc
// @Summary      Get dictionary items by type code
// @Description  Retrieve active dictionary items by type code (convenience API)
// @Tags         Dictionary
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        code   path      string  true  "Dictionary type code"
// @Success      200    {object}  domain.Response{data=[]domain.DictItem}  "Dictionary items retrieved successfully"
// @Failure      500    {object}  domain.Response  "Internal server error"
// @Router       /system/dict/types/code/{code}/items [get]
func (dc *DictController) GetDictItemsByTypeCode(c *gin.Context) {
	code := c.Param("code")

	items, err := dc.DictUseCase.GetActiveItemsByTypeCode(c, code)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess(items))
}
