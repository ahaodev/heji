package controller

import (
	"net/http"
	"shadmin/domain"
	"shadmin/internal/contextutil"

	"github.com/gin-gonic/gin"
)

// CategoryController handles category-related HTTP requests
type CategoryController struct {
	CategoryUsecase domain.CategoryUseCase
}

// CreateCategory godoc
// @Summary 创建分类
// @Tags category
// @Security BearerAuth
// @Param request body domain.CreateCategoryRequest true "创建分类请求"
// @Success 201 {object} domain.Response
// @Router /category [post]
func (cc *CategoryController) CreateCategory(c *gin.Context) {
	var req domain.CreateCategoryRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err))
		return
	}

	userID := contextutil.GetUserID(c)
	cat, err := cc.CategoryUsecase.CreateCategory(c, userID, &req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}
	c.JSON(http.StatusCreated, domain.RespSuccess(cat))
}

// CreateCategories godoc
// @Summary 批量创建分类
// @Tags category
// @Security BearerAuth
// @Param request body []domain.CreateCategoryRequest true "批量创建分类请求"
// @Success 201 {object} domain.Response
// @Router /category/batch [post]
func (cc *CategoryController) CreateCategories(c *gin.Context) {
	var reqs []*domain.CreateCategoryRequest
	if err := c.ShouldBindJSON(&reqs); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err))
		return
	}

	userID := contextutil.GetUserID(c)
	cats, err := cc.CategoryUsecase.CreateCategories(c, userID, reqs)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}
	c.JSON(http.StatusCreated, domain.RespSuccess(cats))
}

// GetCategory godoc
// @Summary 获取分类详情
// @Tags category
// @Security BearerAuth
// @Param id path string true "分类ID"
// @Success 200 {object} domain.Response
// @Router /category/{id} [get]
func (cc *CategoryController) GetCategory(c *gin.Context) {
	id := c.Param("id")
	cat, err := cc.CategoryUsecase.GetCategory(c, id)
	if err != nil {
		c.JSON(http.StatusNotFound, domain.RespError(err))
		return
	}
	c.JSON(http.StatusOK, domain.RespSuccess(cat))
}

// ListCategories godoc
// @Summary 获取账本下的分类列表
// @Tags category
// @Security BearerAuth
// @Param book_id query string true "账本ID"
// @Success 200 {object} domain.Response
// @Router /category [get]
func (cc *CategoryController) ListCategories(c *gin.Context) {
	bookID := c.Query("book_id")
	if bookID == "" {
		c.JSON(http.StatusBadRequest, domain.RespError("book_id is required"))
		return
	}

	cats, err := cc.CategoryUsecase.ListCategories(c, bookID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}
	c.JSON(http.StatusOK, domain.RespSuccess(cats))
}

// UpdateCategory godoc
// @Summary 更新分类
// @Tags category
// @Security BearerAuth
// @Param id path string true "分类ID"
// @Param request body domain.UpdateCategoryRequest true "更新分类请求"
// @Success 200 {object} domain.Response
// @Router /category/{id} [put]
func (cc *CategoryController) UpdateCategory(c *gin.Context) {
	id := c.Param("id")
	var req domain.UpdateCategoryRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err))
		return
	}

	cat, err := cc.CategoryUsecase.UpdateCategory(c, id, &req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}
	c.JSON(http.StatusOK, domain.RespSuccess(cat))
}

// DeleteCategory godoc
// @Summary 删除分类
// @Tags category
// @Security BearerAuth
// @Param id path string true "分类ID"
// @Success 200 {object} domain.Response
// @Router /category/{id} [delete]
func (cc *CategoryController) DeleteCategory(c *gin.Context) {
	id := c.Param("id")
	if err := cc.CategoryUsecase.DeleteCategory(c, id); err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}
	c.JSON(http.StatusOK, domain.RespSuccess(nil))
}
