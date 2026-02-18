package controller

import (
	"errors"
	"net/http"
	bootstrap "shadmin/bootstrap"
	"shadmin/internal/constants"

	"shadmin/domain"

	"github.com/gin-gonic/gin"
)

type ProfileController struct {
	ProfileUsecase domain.ProfileUsecase
	Env            *bootstrap.Env
}

// GetProfile godoc
// @Summary      Get current user profile
// @Description  Get profile information for the currently authenticated user including roles and permissions
// @Tags         Profile
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Success      200  {object}  domain.Response{data=domain.Profile}  "Profile retrieved successfully"
// @Failure      404  {object}  domain.Response  "User not found"
// @Router       /profile [get]
func (pc *ProfileController) GetProfile(c *gin.Context) {
	userID := c.GetString(constants.UserID)
	profile, err := pc.ProfileUsecase.GetProfile(c, userID)
	if err != nil {
		c.JSON(http.StatusNotFound, domain.RespError("User not found"))
		return
	}
	c.JSON(http.StatusOK, domain.RespSuccess(profile))
}

// UpdateProfile godoc
// @Summary      Update current user profile
// @Description  Update profile information for the currently authenticated user
// @Tags         Profile
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        profile  body      domain.ProfileUpdate  true  "Profile update data"
// @Success      200  {object}  domain.Response  "Profile updated successfully"
// @Failure      400  {object}  domain.Response  "Invalid request data"
// @Failure      500  {object}  domain.Response  "Internal server error"
// @Router       /profile [put]
func (pc *ProfileController) UpdateProfile(c *gin.Context) {
	userID := c.GetString(constants.UserID)
	var updateData domain.ProfileUpdate

	if err := c.ShouldBindJSON(&updateData); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err.Error()))
		return
	}

	if err := pc.ProfileUsecase.UpdateProfile(c, userID, updateData); err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess("Profile updated successfully"))
}

// UpdatePassword godoc
// @Summary      Update current user password
// @Description  Update password for the currently authenticated user
// @Tags         Profile
// @Accept       json
// @Produce      json
// @Security     BearerAuth
// @Param        password  body      domain.PasswordUpdate  true  "Password update data"
// @Success      200  {object}  domain.Response  "Password updated successfully"
// @Failure      400  {object}  domain.Response  "Invalid request data or incorrect current password"
// @Failure      500  {object}  domain.Response  "Internal server error"
// @Router       /profile/password [put]
func (pc *ProfileController) UpdatePassword(c *gin.Context) {
	userID := c.GetString(constants.UserID)
	var passwordUpdate domain.PasswordUpdate

	if err := c.ShouldBindJSON(&passwordUpdate); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err.Error()))
		return
	}

	if err := pc.ProfileUsecase.UpdatePassword(c, userID, passwordUpdate); err != nil {
		if errors.Is(err, domain.ErrInvalidPassword) {
			c.JSON(http.StatusBadRequest, domain.RespError("Current password is incorrect"))
			return
		}
		c.JSON(http.StatusInternalServerError, domain.RespError(err.Error()))
		return
	}

	c.JSON(http.StatusOK, domain.RespSuccess("Password updated successfully"))
}
