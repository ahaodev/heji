package controller

import (
	"net/http"
	"shadmin/domain"
	"shadmin/internal/contextutil"
	"strconv"

	"github.com/gin-gonic/gin"
)

// SyncController handles sync-related HTTP requests
type SyncController struct {
	SyncRepo domain.SyncRepository
}

// GetChanges godoc
// @Summary 增量拉取变更
// @Tags sync
// @Security BearerAuth
// @Param since query int true "上次同步时间戳（毫秒）"
// @Param limit query int false "每页数量，默认100"
// @Success 200 {object} domain.Response
// @Router /sync/changes [get]
func (sc *SyncController) GetChanges(c *gin.Context) {
	sinceStr := c.Query("since")
	since, err := strconv.ParseInt(sinceStr, 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError("since parameter is required and must be a valid timestamp in milliseconds"))
		return
	}

	limit := 100
	if limitStr := c.Query("limit"); limitStr != "" {
		if v, err := strconv.Atoi(limitStr); err == nil && v > 0 && v <= 500 {
			limit = v
		}
	}

	userID := contextutil.GetUserID(c)
	result, err := sc.SyncRepo.QueryChanges(c, userID, since, limit)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}
	c.JSON(http.StatusOK, domain.RespSuccess(result))
}
