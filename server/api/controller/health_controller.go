package controller

import (
	"net/http"
	"shadmin/domain"

	"github.com/gin-gonic/gin"
)

// HealthController handles health check requests
type HealthController struct{}

// Health GoDoc
// @Summary health check
// @Description  Returns service health status
// @Tags         Health
// @Produce      json
// @Success      200  {object}  domain.Response
// @Router       /health [get]
func (hc *HealthController) Health(c *gin.Context) {
	c.JSON(http.StatusOK, domain.RespSuccess(nil))
}
