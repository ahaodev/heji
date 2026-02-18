package controller

import (
	"fmt"
	"net/http"
	"path/filepath"
	"shadmin/domain"

	"github.com/gin-gonic/gin"
)

// ImageController handles bill image-related HTTP requests
type ImageController struct {
	ImageUsecase domain.BillImageUseCase
	FileRepo     domain.FileRepository
}

// UploadImage godoc
// @Summary 上传账单图片
// @Tags image
// @Security BearerAuth
// @Param file formance file true "图片文件"
// @Param billId query string true "账单ID"
// @Success 201 {object} domain.Response
// @Router /image/upload [post]
func (ic *ImageController) UploadImage(c *gin.Context) {
	billID := c.Query("billId")
	if billID == "" {
		c.JSON(http.StatusBadRequest, domain.RespError("billId is required"))
		return
	}

	file, header, err := c.Request.FormFile("file")
	if err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(fmt.Errorf("failed to get file: %w", err)))
		return
	}
	defer file.Close()

	ext := filepath.Ext(header.Filename)
	objectName := fmt.Sprintf("bills/%s/%s", billID, header.Filename)

	upload := &domain.UploadRequest{
		Bucket:      "heji",
		ObjectName:  objectName,
		Reader:      file,
		Size:        header.Size,
		ContentType: header.Header.Get("Content-Type"),
	}

	img := &domain.BillImage{
		ID:  c.Query("_id"),
		Ext: ext,
	}

	result, err := ic.ImageUsecase.UploadImage(c, billID, img, upload)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}
	c.JSON(http.StatusCreated, domain.RespSuccess(result))
}

// GetBillImages godoc
// @Summary 获取账单的图片列表
// @Tags image
// @Security BearerAuth
// @Param bill_id query string true "账单ID"
// @Success 200 {object} domain.Response
// @Router /image/list [get]
func (ic *ImageController) GetBillImages(c *gin.Context) {
	billID := c.Query("bill_id")
	if billID == "" {
		c.JSON(http.StatusBadRequest, domain.RespError("bill_id is required"))
		return
	}

	images, err := ic.ImageUsecase.ListByBillID(c, billID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}
	c.JSON(http.StatusOK, domain.RespSuccess(images))
}

// GetImage godoc
// @Summary 获取图片详情/下载图片
// @Tags image
// @Security BearerAuth
// @Param id path string true "图片ID"
// @Success 200 {file} binary
// @Router /image/{id} [get]
func (ic *ImageController) GetImage(c *gin.Context) {
	id := c.Param("id")
	img, err := ic.ImageUsecase.GetImage(c, id)
	if err != nil {
		c.JSON(http.StatusNotFound, domain.RespError(err))
		return
	}

	reader, err := ic.FileRepo.Download(c, &domain.DownloadRequest{
		Bucket:     "heji",
		ObjectName: img.Key,
	})
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}
	defer reader.Close()

	contentType := "application/octet-stream"
	if img.Ext != "" {
		switch img.Ext {
		case ".jpg", ".jpeg":
			contentType = "image/jpeg"
		case ".png":
			contentType = "image/png"
		case ".gif":
			contentType = "image/gif"
		case ".webp":
			contentType = "image/webp"
		}
	}

	c.DataFromReader(http.StatusOK, img.Size, contentType, reader, nil)
}

// DeleteImage godoc
// @Summary 删除账单图片
// @Tags image
// @Security BearerAuth
// @Param billId query string true "账单ID"
// @Param imageId query string true "图片ID"
// @Success 200 {object} domain.Response
// @Router /image [delete]
func (ic *ImageController) DeleteImage(c *gin.Context) {
	billID := c.Query("billId")
	imageID := c.Query("imageId")
	if billID == "" || imageID == "" {
		c.JSON(http.StatusBadRequest, domain.RespError("billId and imageId are required"))
		return
	}

	if err := ic.ImageUsecase.DeleteImage(c, billID, imageID); err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}
	c.JSON(http.StatusOK, domain.RespSuccess(nil))
}
