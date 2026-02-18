package controller

import (
	"net/http"
	bootstrap "shadmin/bootstrap"
	"strconv"
	"time"

	"shadmin/domain"

	"github.com/gin-gonic/gin"
)

type LoginLogController struct {
	LoginLogUsecase domain.LoginLogUseCase
	Env             *bootstrap.Env
}

// GetLoginLogs godoc
// @Summary      Get login logs
// @Description  Retrieve login logs with pagination and optional filtering (admin only)
// @Tags         System
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        page      query  int     false  "Page number (default: 1)"
// @Param        page_size query  int     false  "Page size (default: 20)"
// @Param        username  query  string  false  "Filter by username"
// @Param        login_ip  query  string  false  "Filter by login IP"
// @Param        status    query  string  false  "Filter by status (success, failed)"
// @Param        browser   query  string  false  "Filter by browser"
// @Param        os        query  string  false  "Filter by operating system"
// @Param        start_time query string false  "Start time (RFC3339 format, e.g., 2023-01-01T00:00:00Z)"
// @Param        end_time   query string false  "End time (RFC3339 format, e.g., 2023-01-31T23:59:59Z)"
// @Param        sort_by   query  string  false  "Sort by field (login_time, username, login_ip, status)"
// @Param        order     query  string  false  "Sort order (asc, desc, default: desc)"
// @Success      200       {object}  domain.Response{data=domain.LoginLogPagedResult}  "Login logs retrieved successfully"
// @Failure      400       {object}  domain.Response  "Invalid request parameters"
// @Failure      500       {object}  domain.Response  "Internal server error"
// @Router       /system/login-logs [get]
func (llc *LoginLogController) GetLoginLogs(c *gin.Context) {
	// 构建查询过滤器
	var filter domain.LoginLogQueryFilter

	// 解析分页参数
	if p := c.Query("page"); p != "" {
		if v, err := strconv.Atoi(p); err == nil && v > 0 {
			filter.Page = v
		} else {
			filter.Page = 1
		}
	} else {
		filter.Page = 1
	}

	if ps := c.Query("page_size"); ps != "" {
		if v, err := strconv.Atoi(ps); err == nil && v > 0 {
			filter.PageSize = v
		} else {
			filter.PageSize = 20
		}
	} else {
		filter.PageSize = 20
	}

	// 解析过滤参数
	filter.Username = c.Query("username")
	filter.LoginIP = c.Query("login_ip")
	filter.Status = c.Query("status")
	filter.Browser = c.Query("browser")
	filter.OS = c.Query("os")

	// 解析时间范围参数
	if startTime := c.Query("start_time"); startTime != "" {
		if t, err := time.Parse(time.RFC3339, startTime); err == nil {
			filter.StartTime = &t
		} else {
			c.JSON(http.StatusBadRequest, domain.RespError("Invalid start_time format, please use RFC3339 format (e.g., 2023-01-01T00:00:00Z)"))
			return
		}
	}

	if endTime := c.Query("end_time"); endTime != "" {
		if t, err := time.Parse(time.RFC3339, endTime); err == nil {
			filter.EndTime = &t
		} else {
			c.JSON(http.StatusBadRequest, domain.RespError("Invalid end_time format, please use RFC3339 format (e.g., 2023-01-31T23:59:59Z)"))
			return
		}
	}

	// 解析排序参数
	filter.SortBy = c.Query("sort_by")
	filter.Order = c.Query("order")
	if filter.Order == "" {
		filter.Order = "desc"
	}

	// 调用用例获取登录日志
	result, err := llc.LoginLogUsecase.ListLoginLogs(c, filter)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError("Failed to retrieve login logs: "+err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess(result))
}

// ClearLoginLogs godoc
// @Summary      Clear all login logs
// @Description  Clear all login logs (admin only)
// @Tags         System
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Success      200  {object}  domain.Response  "Login logs cleared successfully"
// @Failure      500  {object}  domain.Response  "Internal server error"
// @Router       /system/login-logs [delete]
func (llc *LoginLogController) ClearLoginLogs(c *gin.Context) {
	err := llc.LoginLogUsecase.ClearAllLoginLogs(c)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError("Failed to clear login logs: "+err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess("All login logs have been cleared successfully"))
}
