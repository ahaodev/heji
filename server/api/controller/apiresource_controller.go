package controller

import (
	"net/http"
	"shadmin/domain"
	"strconv"

	"github.com/gin-gonic/gin"
)

type ApiResourceController struct {
	ApiResourceUseCase domain.ApiResourceUseCase
}

// GetApiResources godoc
// @Summary      Get API resources with pagination
// @Description  Retrieve a list of API resources with optional filtering
// @Tags         API Resources
// @Accept       json
// @Produce      json
// @Param        page      query     int     false  "Page number (default: 1)"
// @Param        page_size query     int     false  "Page size (default: 10)"
// @Param        method    query     string  false  "Filter by HTTP method"
// @Param        module    query     string  false  "Filter by module"
// @Param        status    query     string  false  "Filter by status"
// @Param        is_public query     bool    false  "Filter by public status"
// @Param        keyword   query     string  false  "Search keyword"
// @Param        path      query     string  false  "Filter by path"
// @Success      200  {object}  domain.Response{data=domain.ApiResourcePagedResult}  "Success"
// @Failure      400  {object}  domain.Response  "Invalid parameters"
// @Failure      500  {object}  domain.Response  "Internal server error"
// @Router       /api-resources [get]
func (arc *ApiResourceController) GetApiResources(c *gin.Context) {
	page, _ := strconv.Atoi(c.DefaultQuery("page", "1"))
	pageSize, _ := strconv.Atoi(c.DefaultQuery("page_size", "10"))
	method := c.Query("method")
	module := c.Query("module")
	status := c.Query("status")
	keyword := c.Query("keyword")
	path := c.Query("path")

	var isPublic *bool
	if publicStr := c.Query("is_public"); publicStr != "" {
		if publicBool, err := strconv.ParseBool(publicStr); err == nil {
			isPublic = &publicBool
		}
	}

	params := domain.ApiResourceQueryParams{
		QueryParams: domain.QueryParams{
			Page:     page,
			PageSize: pageSize,
		},
		Method:   method,
		Module:   module,
		Status:   status,
		IsPublic: isPublic,
		Keyword:  keyword,
		Path:     path,
	}

	result, err := arc.ApiResourceUseCase.FetchPaged(c, params)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess(result))
}
