package controller

import (
	"net/http"
	"strconv"

	"shadmin/domain"

	"github.com/gin-gonic/gin"
)

// BindQueryParams extracts common pagination and sorting query parameters from the request.
func BindQueryParams(c *gin.Context) domain.QueryParams {
	var params domain.QueryParams
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
	params.SortBy = c.Query("sort_by")
	params.Order = c.Query("order")
	_ = domain.ValidateQueryParams(&params)
	return params
}

// MustBindJSON binds JSON body to the target and writes a 400 error response on failure.
// Returns true if binding succeeded, false if an error response was already written.
func MustBindJSON(c *gin.Context, obj interface{}) bool {
	if err := c.ShouldBindJSON(obj); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err.Error()))
		return false
	}
	return true
}
