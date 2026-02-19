package controller

import (
	"encoding/json"
	"fmt"
	"net/http"
	"shadmin/domain"
	"shadmin/internal/contextutil"
	"shadmin/internal/sync"
	"strconv"

	"github.com/gin-gonic/gin"
)

// BillController handles bill-related HTTP requests
type BillController struct {
	BillUsecase    domain.BillUseCase
	UserRepository domain.UserRepository
	SyncService    *sync.SyncService
}

// CreateBill godoc
// @Summary 创建账单
// @Tags bill
// @Security BearerAuth
// @Param request body domain.CreateBillRequest true "创建账单请求"
// @Success 201 {object} domain.Response
// @Router /bill [post]
func (bc *BillController) CreateBill(c *gin.Context) {
	var req domain.CreateBillRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err))
		return
	}

	userID := contextutil.GetUserID(c)
	bill, err := bc.BillUsecase.CreateBill(c, userID, &req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}

	// 推送通知给账本其他成员
	if bc.SyncService != nil {
		billJSON, _ := json.Marshal(bill)
		go bc.SyncService.NotifyBookMembers(req.BookID, userID, "ADD_BILL", "bill", string(billJSON))
	}

	c.JSON(http.StatusCreated, domain.RespSuccess(bill))
}

// GetBill godoc
// @Summary 获取账单详情
// @Tags bill
// @Security BearerAuth
// @Param id path string true "账单ID"
// @Success 200 {object} domain.Response
// @Router /bill/{id} [get]
func (bc *BillController) GetBill(c *gin.Context) {
	id := c.Param("id")
	bill, err := bc.BillUsecase.GetBill(c, id)
	if err != nil {
		c.JSON(http.StatusNotFound, domain.RespError(err))
		return
	}
	c.JSON(http.StatusOK, domain.RespSuccess(bill))
}

// ListBills godoc
// @Summary 查询账单列表
// @Tags bill
// @Security BearerAuth
// @Param book_id query string true "账本ID"
// @Param type query int false "账单类型"
// @Param category query string false "分类"
// @Param time_from query string false "起始日期"
// @Param time_to query string false "结束日期"
// @Param page query int false "页码"
// @Param page_size query int false "每页数量"
// @Success 200 {object} domain.Response
// @Router /bill [get]
func (bc *BillController) ListBills(c *gin.Context) {
	var filter domain.BillQueryFilter
	filter.BookID = c.Query("book_id")
	if filter.BookID == "" {
		c.JSON(http.StatusBadRequest, domain.RespError("book_id is required"))
		return
	}
	filter.Category = c.Query("category")
	filter.TimeFrom = c.Query("time_from")
	filter.TimeTo = c.Query("time_to")

	if t := c.Query("type"); t != "" {
		v, _ := strconv.Atoi(t)
		filter.Type = &v
	}
	if p := c.Query("page"); p != "" {
		filter.Page, _ = strconv.Atoi(p)
	}
	if ps := c.Query("page_size"); ps != "" {
		filter.PageSize, _ = strconv.Atoi(ps)
	}
	_ = domain.ValidateQueryParams(&filter.QueryParams)

	result, err := bc.BillUsecase.ListBills(c, filter)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}

	// Resolve user IDs to usernames
	if bc.UserRepository != nil {
		userIDs := make(map[string]struct{})
		for _, bill := range result.List {
			userIDs[bill.CrtUser] = struct{}{}
		}
		userMap := make(map[string]string, len(userIDs))
		for uid := range userIDs {
			if u, err := bc.UserRepository.GetByID(c, uid); err == nil {
				userMap[uid] = u.Username
			}
		}
		for _, bill := range result.List {
			if name, ok := userMap[bill.CrtUser]; ok {
				bill.CrtUserName = name
			}
		}
	}

	c.JSON(http.StatusOK, domain.RespSuccess(result))
}

// UpdateBill godoc
// @Summary 更新账单
// @Tags bill
// @Security BearerAuth
// @Param id path string true "账单ID"
// @Param request body domain.UpdateBillRequest true "更新账单请求"
// @Success 200 {object} domain.Response
// @Router /bill/{id} [put]
func (bc *BillController) UpdateBill(c *gin.Context) {
	id := c.Param("id")
	var req domain.UpdateBillRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, domain.RespError(err))
		return
	}

	bill, err := bc.BillUsecase.UpdateBill(c, id, &req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}

	// 推送通知
	if bc.SyncService != nil {
		billJSON, _ := json.Marshal(bill)
		userID := contextutil.GetUserID(c)
		go bc.SyncService.NotifyBookMembers(bill.BookID, userID, "UPDATE_BILL", "bill", string(billJSON))
	}

	c.JSON(http.StatusOK, domain.RespSuccess(bill))
}

// DeleteBill godoc
// @Summary 删除账单
// @Tags bill
// @Security BearerAuth
// @Param id path string true "账单ID"
// @Success 200 {object} domain.Response
// @Router /bill/{id} [delete]
func (bc *BillController) DeleteBill(c *gin.Context) {
	id := c.Param("id")

	// 获取账单信息（推送通知需要 bookID）
	bill, _ := bc.BillUsecase.GetBill(c, id)

	if err := bc.BillUsecase.DeleteBill(c, id); err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}

	// 推送通知
	if bc.SyncService != nil && bill != nil {
		userID := contextutil.GetUserID(c)
		go bc.SyncService.NotifyBookMembers(bill.BookID, userID, "DELETE_BILL", "bill", id)
	}

	c.JSON(http.StatusOK, domain.RespSuccess(nil))
}

// ExportBills godoc
// @Summary 导出账单
// @Tags bill
// @Security BearerAuth
// @Param book_id query string true "账本ID"
// @Param year query string false "年份"
// @Param month query string false "月份"
// @Success 200 {file} binary
// @Router /bill/export [get]
func (bc *BillController) ExportBills(c *gin.Context) {
	bookID := c.Query("book_id")
	if bookID == "" {
		c.JSON(http.StatusBadRequest, domain.RespError("book_id is required"))
		return
	}

	var filter domain.BillQueryFilter
	filter.BookID = bookID
	filter.PageSize = 10000

	year := c.Query("year")
	month := c.Query("month")
	if year != "" && month != "" {
		filter.TimeFrom = fmt.Sprintf("%s-%s-01", year, month)
		filter.TimeTo = fmt.Sprintf("%s-%s-31", year, month)
	} else if year != "" {
		filter.TimeFrom = year + "-01-01"
		filter.TimeTo = year + "-12-31"
	}

	_ = domain.ValidateQueryParams(&filter.QueryParams)
	result, err := bc.BillUsecase.ListBills(c, filter)
	if err != nil {
		c.JSON(http.StatusInternalServerError, domain.RespError(err))
		return
	}

	// 生成CSV
	c.Header("Content-Type", "text/csv; charset=utf-8")
	c.Header("Content-Disposition", "attachment; filename=bills.csv")
	// BOM for Excel UTF-8
	_, _ = c.Writer.Write([]byte{0xEF, 0xBB, 0xBF})
	_, _ = c.Writer.WriteString("日期,类型,分类,金额,备注\n")
	for _, bill := range result.List {
		typeName := "支出"
		if bill.Type == 1 {
			typeName = "收入"
		}
		line := fmt.Sprintf("%s,%s,%s,%.2f,%s\n",
			bill.Time, typeName, bill.Category, float64(bill.Money)/100, bill.Remark)
		_, _ = c.Writer.WriteString(line)
	}
}
