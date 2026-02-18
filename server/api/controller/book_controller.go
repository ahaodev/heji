package controller

import (
	"encoding/json"
	"net/http"
	"shadmin/domain"
	"shadmin/internal/contextutil"
	"shadmin/internal/sync"

	"github.com/gin-gonic/gin"
)

// BookController handles book-related HTTP requests
type BookController struct {
	BookUsecase domain.BookUseCase
	SyncService *sync.SyncService
}

// CreateBook godoc
// @Summary 创建账本
// @Tags book
// @Security BearerAuth
// @Param request body domain.CreateBookRequest true "创建账本请求"
// @Success 201 {object} domain.Response
// @Router /book [post]
func (bc *BookController) CreateBook(c *gin.Context) {
	var req domain.CreateBookRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err))
		return
	}

	userID := contextutil.GetUserID(c)
	book, err := bc.BookUsecase.CreateBook(c, userID, &req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}

	if bc.SyncService != nil {
		bookJSON, _ := json.Marshal(book)
		go bc.SyncService.NotifyBookMembers(book.ID, userID, "ADD_BOOK", "book", string(bookJSON))
	}

	c.JSON(http.StatusCreated, domain.RespSuccess(book))
}

// GetBook godoc
// @Summary 获取账本详情
// @Tags book
// @Security BearerAuth
// @Param id path string true "账本ID"
// @Success 200 {object} domain.Response
// @Router /book/{id} [get]
func (bc *BookController) GetBook(c *gin.Context) {
	id := c.Param("id")
	book, err := bc.BookUsecase.GetBook(c, id)
	if err != nil {
		c.JSON(http.StatusNotFound, domain.RespError(err))
		return
	}
	c.JSON(http.StatusOK, domain.RespSuccess(book))
}

// ListBooks godoc
// @Summary 获取当前用户的账本列表
// @Tags book
// @Security BearerAuth
// @Success 200 {object} domain.Response
// @Router /book [get]
func (bc *BookController) ListBooks(c *gin.Context) {
	userID := contextutil.GetUserID(c)
	books, err := bc.BookUsecase.ListBooks(c, userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}
	c.JSON(http.StatusOK, domain.RespSuccess(books))
}

// UpdateBook godoc
// @Summary 更新账本
// @Tags book
// @Security BearerAuth
// @Param id path string true "账本ID"
// @Param request body domain.UpdateBookRequest true "更新账本请求"
// @Success 200 {object} domain.Response
// @Router /book/{id} [put]
func (bc *BookController) UpdateBook(c *gin.Context) {
	id := c.Param("id")
	var req domain.UpdateBookRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err))
		return
	}

	book, err := bc.BookUsecase.UpdateBook(c, id, &req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}

	if bc.SyncService != nil {
		bookJSON, _ := json.Marshal(book)
		userID := contextutil.GetUserID(c)
		go bc.SyncService.NotifyBookMembers(book.ID, userID, "UPDATE_BOOK", "book", string(bookJSON))
	}

	c.JSON(http.StatusOK, domain.RespSuccess(book))
}

// DeleteBook godoc
// @Summary 删除账本
// @Tags book
// @Security BearerAuth
// @Param id path string true "账本ID"
// @Success 200 {object} domain.Response
// @Router /book/{id} [delete]
func (bc *BookController) DeleteBook(c *gin.Context) {
	id := c.Param("id")
	if err := bc.BookUsecase.DeleteBook(c, id); err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}

	if bc.SyncService != nil {
		userID := contextutil.GetUserID(c)
		go bc.SyncService.NotifyBookMembers(id, userID, "DELETE_BOOK", "book", id)
	}

	c.JSON(http.StatusOK, domain.RespSuccess(nil))
}

// JoinBook godoc
// @Summary 加入账本
// @Tags book
// @Security BearerAuth
// @Param id path string true "账本ID"
// @Success 200 {object} domain.Response
// @Router /book/{id}/join [post]
func (bc *BookController) JoinBook(c *gin.Context) {
	bookID := c.Param("id")
	userID := contextutil.GetUserID(c)
	if err := bc.BookUsecase.JoinBook(c, bookID, userID); err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}
	c.JSON(http.StatusOK, domain.RespSuccess(nil))
}

// ShareBook godoc
// @Summary 分享账本（生成分享码）
// @Tags book
// @Security BearerAuth
// @Param id path string true "账本ID"
// @Success 200 {object} domain.Response
// @Router /book/{id}/share [post]
func (bc *BookController) ShareBook(c *gin.Context) {
	bookID := c.Param("id")
	code, err := bc.BookUsecase.ShareBook(c, bookID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}
	c.JSON(http.StatusOK, domain.RespSuccess(code))
}
